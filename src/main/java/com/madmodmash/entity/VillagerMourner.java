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

/** HmmMod villager mourner: appears when villagers die, sings for them, and hands out tissues. */
public class VillagerMourner extends PathfinderMob {
    int singCooldown = 0;
    public VillagerMourner(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() { return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.4); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Monster.class, 8F, 0.6, 0.7));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8F));
    }
    public static void onVillagerDeath(ServerLevel level, LivingEntity villager) {
        var mourners = level.getEntitiesOfClass(VillagerMourner.class, villager.getBoundingBox().inflate(48));
        VillagerMourner m = mourners.isEmpty() ? (level.getRandom().nextInt(3) == 0 ? Util2.spawn(level, ModEntities.VILLAGER_MOURNER.get(), Util2.surface(level, villager.blockPosition(), 3, 8)) : null) : mourners.get(0);
        if (m != null) m.sing();
    }
    public void sing() {
        if (singCooldown > 0 || !(level() instanceof ServerLevel sl)) return;
        singCooldown = 2400;
        sl.playSound(null, blockPosition(), ModSounds.SAD_SONG.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.5F, 1F);
        sl.sendParticles(ParticleTypes.NOTE, getX(), getY() + 2.2, getZ(), 6, 0.5, 0.3, 0.5, 1);
    }
    @Override public void aiStep() { super.aiStep(); if (singCooldown > 0) singCooldown--; }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            Util2.give(player, new ItemStack(ModItems.TISSUE.get()));
            if (player instanceof ServerPlayer sp) Quests.onScan(sp, this);
        }
        return InteractionResult.SUCCESS;
    }
}
