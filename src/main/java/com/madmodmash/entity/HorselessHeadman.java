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

/** Nay: the rider, unhorsed and still headless, with flaming heads at his call. */
public class HorselessHeadman extends MashMonster {
    int heads = 100;
    public HorselessHeadman(EntityType<? extends Monster> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return MashMonster.createAttributes().add(Attributes.MAX_HEALTH, 90).add(Attributes.ATTACK_DAMAGE, 9).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ARMOR, 6).add(Attributes.FOLLOW_RANGE, 40);
    }
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d, EntitySpawnReason reason, SpawnGroupData data) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.IRON_SCYTHE.get()));
        return super.finalizeSpawn(level, d, reason, data);
    }
    @Override public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel sl && getTarget() != null && --heads <= 0) {
            heads = 140;
            if (sl.getEntitiesOfClass(LivingHead.class, getBoundingBox().inflate(20)).size() < 4) Util2.spawn(sl, ModEntities.LIVING_HEAD.get(), blockPosition().above(2));
        }
    }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) { boolean hit = super.doHurtTarget(level, target); if (hit) target.igniteForSeconds(4); return hit; }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource s, boolean byPlayer) {
        super.dropCustomDeathLoot(level, s, byPlayer);
        spawnAtLocation(level, new ItemStack(ModItems.CURSED_PUMPKIN.get()));
        Engrams.spawn(level, position(), 15);
    }
}
