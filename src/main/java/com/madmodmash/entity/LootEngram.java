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

/** WildLoot loot engram: floating, glowing loot. Hit it or use it to decrypt. */
public class LootEngram extends Entity {
    static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(LootEngram.class, EntityDataSerializers.INT);
    public LootEngram(EntityType<?> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { b.define(TIER, 0); }
    public int getTier() { return entityData.get(TIER); }
    public void setTier(int t) { entityData.set(TIER, t); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        setDeltaMovement(getDeltaMovement().scale(0.9));
        if (getDeltaMovement().lengthSqr() > 1.0E-6) move(MoverType.SELF, getDeltaMovement());
        if (level() instanceof ServerLevel sl) {
            if (tickCount % 10 == 0) sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.8, getZ(), 1, 0.2, 0.2, 0.2, 0.01);
            if (tickCount > 12000) discard();
        }
    }
    @Override public boolean isPickable() { return true; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) {
        if (src.getEntity() instanceof Player p) decrypt(level, p);
        return true;
    }
    @Override public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (level() instanceof ServerLevel sl) decrypt(sl, player);
        return InteractionResult.SUCCESS;
    }
    void decrypt(ServerLevel level, Player p) {
        if (isRemoved()) return;
        String tier = Engrams.TIERS[Math.max(0, Math.min(Engrams.TIERS.length - 1, getTier()))];
        LootHelper.drop(level, "engram/" + tier, position().add(0, 0.5, 0));
        level.sendParticles(ParticleTypes.FIREWORK, getX(), getY() + 0.5, getZ(), 30, 0.3, 0.3, 0.3, 0.1);
        level.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.PLAYERS, 1F, 1.2F);
        p.sendOverlayMessage(Component.translatable("msg.madmodmash.engram", Component.translatable("engram.madmodmash." + tier)).withStyle(Engrams.COLORS[getTier()]));
        discard();
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { out.putInt("Tier", getTier()); }
    @Override protected void readAdditionalSaveData(ValueInput in) { setTier(in.getIntOr("Tier", 0)); }
    @Override public boolean shouldBeSaved() { return true; }
}
