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

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
/** A thrown knife: hits, then drops where it lands to be picked up again. */
public class ThrownKnife extends ThrowableItemProjectile {
    public ThrownKnife(EntityType<? extends ThrownKnife> type, Level level) { super(type, level); }
    public ThrownKnife(EntityType<? extends ThrownKnife> type, LivingEntity owner, Level level, ItemStack stack) { super(type, owner, level, stack); }
    @Override protected Item getDefaultItem() { return ModItems.IRON_KNIFE.get(); }
    public static void throwKnife(ServerLevel level, Player p, ItemStack knife) {
        ThrownKnife k = new ThrownKnife(ModEntities.THROWN_KNIFE.get(), p, level, knife);
        k.shootFromRotation(p, p.getXRot(), p.getYRot(), 0F, 2.2F, 0.5F);
        level.addFreshEntity(k);
    }
    float damage() {
        var mods = getItem().get(DataComponents.ATTRIBUTE_MODIFIERS);
        double d = 4;
        if (mods != null) for (var e : mods.modifiers()) if (e.attribute().is(Attributes.ATTACK_DAMAGE)) d = 1 + e.modifier().amount() * 1.5;
        return (float) d;
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (level() instanceof ServerLevel sl && hit.getEntity() instanceof LivingEntity t) {
            t.hurtServer(sl, sl.damageSources().thrown(this, getOwner()), damage());
            Trait tr = Trait.of(getItem());
            if (tr != null && getOwner() instanceof LivingEntity o) tr.onHit(sl, o, t, Trait.level(getItem()), damage());
        }
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel sl) { Util2.drop(sl, getX(), getY(), getZ(), getItem().copy()); discard(); }
    }
}
