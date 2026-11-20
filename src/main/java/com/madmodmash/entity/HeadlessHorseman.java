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

/** Nay boss: the Headless Horseman. When he falls, the Horseless Headman rises. */
public class HeadlessHorseman extends MashMonster {
    private final net.minecraft.server.level.ServerBossEvent bar = new net.minecraft.server.level.ServerBossEvent(java.util.UUID.randomUUID(), getDisplayName(),
            net.minecraft.world.BossEvent.BossBarColor.YELLOW, net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
    int skull = 60;
    public HeadlessHorseman(EntityType<? extends Monster> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return MashMonster.createAttributes().add(Attributes.MAX_HEALTH, 180).add(Attributes.ATTACK_DAMAGE, 11).add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.ARMOR, 8).add(Attributes.FOLLOW_RANGE, 48).add(Attributes.STEP_HEIGHT, 1.5);
    }
    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl)) return;
        bar.setProgress(getHealth() / getMaxHealth());
        LivingEntity t = getTarget();
        if (t != null && --skull <= 0) {
            skull = 50 + getRandom().nextInt(40);
            if (getRandom().nextInt(3) == 0 && sl.getEntitiesOfClass(LivingHead.class, getBoundingBox().inflate(20)).size() < 3) Util2.spawn(sl, ModEntities.LIVING_HEAD.get(), blockPosition().above(2));
            else { var ws = new net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull(sl, this, t.position().subtract(position()).normalize()); ws.setPos(getX(), getEyeY(), getZ()); sl.addFreshEntity(ws); playSound(SoundEvents.WITHER_SHOOT, 1F, 0.8F); }
        }
        if (tickCount % 4 == 0) sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 2.6, getZ(), 2, 0.1, 0.1, 0.1, 0.01);
    }
    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bar.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }
    @Override public void die(DamageSource src) {
        if (level() instanceof ServerLevel sl) Util2.spawn(sl, ModEntities.HORSELESS_HEADMAN.get(), blockPosition());
        super.die(src);
    }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return SoundEvents.SKELETON_HORSE_AMBIENT; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s) { return SoundEvents.SKELETON_HORSE_HURT; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.SKELETON_HORSE_DEATH; }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource s, boolean byPlayer) { super.dropCustomDeathLoot(level, s, byPlayer); spawnAtLocation(level, new ItemStack(Items.JACK_O_LANTERN, 2)); }
}
