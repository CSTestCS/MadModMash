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

/** Zombie Plague trudger and NewMobs Atlas zombie: towering plague-bearers that rot the earth. */
public class ZombieTrudger extends MashMonster {
    private final net.minecraft.server.level.ServerBossEvent bar = new net.minecraft.server.level.ServerBossEvent(java.util.UUID.randomUUID(), getDisplayName(),
            net.minecraft.world.BossEvent.BossBarColor.GREEN, net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
    int summon = 200;
    public ZombieTrudger(EntityType<? extends Monster> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return MashMonster.createAttributes().add(Attributes.MAX_HEALTH, 220).add(Attributes.ATTACK_DAMAGE, 14).add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ARMOR, 8).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.STEP_HEIGHT, 1.5);
    }
    public static AttributeSupplier.Builder createAtlasAttributes() { return createAttributes().add(Attributes.MAX_HEALTH, 300).add(Attributes.ATTACK_DAMAGE, 18); }
    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl)) return;
        bar.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 20 == 0) {
            BlockPos c = blockPosition().below();
            for (int i = 0; i < 3; i++) { BlockPos p = c.offset(getRandom().nextInt(7) - 3, 0, getRandom().nextInt(7) - 3);
                if (sl.getBlockState(p).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) sl.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.MYCELIUM.defaultBlockState()); }
        }
        if (getTarget() != null && --summon <= 0) {
            summon = 300;
            for (int i = 0; i < 2; i++) Util2.spawn(sl, ModEntities.MORTIS_ZOMBIE.get(), blockPosition().offset(getRandom().nextInt(7) - 3, 0, getRandom().nextInt(7) - 3));
            for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5), e -> e != this && !(e instanceof MortisZombie))) Util2.knock(e, 1.5, getX() - e.getX(), getZ() - e.getZ());
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 3, 1.5, 0.2, 1.5, 0);
            playSound(SoundEvents.RAVAGER_ROAR, 2F, 0.6F);
        }
    }
    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bar.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity le) le.addEffect(new MobEffectInstance(ModEffects.ZOMBIE_PLAGUE, 1200), this);
        return hit;
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource s, boolean byPlayer) {
        super.dropCustomDeathLoot(level, s, byPlayer);
        spawnAtLocation(level, new ItemStack(ModItems.MORTIS_CURE.get(), 2));
        Engrams.spawn(level, position(), 13);
    }
}
