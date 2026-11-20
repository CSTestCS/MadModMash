package com.madmodmash.entity;

import com.madmodmash.*;
import com.madmodmash.entity.*;
import com.madmodmash.item.*;
import com.madmodmash.block.*;
import com.madmodmash.registry.*;
import com.madmodmash.system.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Quack of Legends nexus. A Redstone Nexus musters redstone minions for the Illager Host;
 * an Emerald Nexus raised by a player musters villager minions for the Empire.
 */
public class Nexus extends PathfinderMob implements Ownable {
    int spawnTimer = 200;
    public Nexus(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 150).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ARMOR, 8);
    }
    boolean emerald() { return getType() == ModEntities.EMERALD_NEXUS.get(); }
    @Override public void setOwnerPlayer(Player p) {}
    @Override public void aiStep() {
        super.aiStep();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (!(level() instanceof ServerLevel sl) || --spawnTimer > 0) return;
        spawnTimer = emerald() ? 400 : 300;
        var minionType = emerald() ? ModEntities.VILLAGER_MINION.get() : ModEntities.REDSTONE_MINION.get();
        if (sl.getEntities(minionType, getBoundingBox().inflate(24), e -> true).size() < (emerald() ? 4 : 6)) {
            Util2.spawn(sl, minionType, Util2.surface(sl, blockPosition(), 1, 3));
            sl.sendParticles(emerald() ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.FLAME, getX(), getY() + 1.5, getZ(), 15, 0.5, 0.5, 0.5, 0.05);
        }
    }
    @Override public boolean isPushable() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource s, boolean b) {
        super.dropCustomDeathLoot(level, s, b);
        if (!emerald()) { spawnAtLocation(level, new ItemStack(ModItems.REDSTONE_CORE.get(), 1)); Engrams.spawn(level, position(), 5); }
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (emerald() && player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (!level().isClientSide()) { Util2.give(player, new ItemStack(ModItems.EMERALD_NEXUS_CORE.get())); discard(); }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}
