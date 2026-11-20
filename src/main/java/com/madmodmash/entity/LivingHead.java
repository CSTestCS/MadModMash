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

/** Nay living head: a flaming pumpkin that hunts you through the air and bursts. */
public class LivingHead extends Monster {
    public LivingHead(EntityType<? extends Monster> type, Level level) { super(type, level); setNoGravity(true); }
    public static AttributeSupplier.Builder createAttributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8).add(Attributes.FOLLOW_RANGE, 32).add(Attributes.FLYING_SPEED, 0.4); }
    @Override protected void registerGoals() { this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false)); }
    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity t = getTarget();
        if (t != null) {
            Vec3 d = t.getEyePosition().subtract(position()).normalize().scale(0.25);
            setDeltaMovement(d);
            lookAt(t, 30, 30);
            if (distanceTo(t) < 1.5) { t.hurtServer(sl, sl.damageSources().mobAttack(this), 6); t.igniteForSeconds(3);
                sl.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 30, 0.3, 0.3, 0.3, 0.1); playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 1.4F); discard(); }
        } else setDeltaMovement(getDeltaMovement().scale(0.8));
        if (tickCount > 1200) discard();
    }
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
}
