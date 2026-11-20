package com.madmodmash.item;

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

/** All the weapon archetypes from the Battle Pass and Medieval weapon notes. */
public class MashWeaponItem extends Item {
    public final WeaponClass cls;
    public MashWeaponItem(Properties p, WeaponClass cls) { super(p); this.cls = cls; }
    public static MashWeaponItem sword(Properties p) { return new MashWeaponItem(p, WeaponClass.SWORD); }

    @Override public float getAttackDamageBonus(Entity victim, float damage, net.minecraft.world.damagesource.DamageSource source) {
        if (cls == WeaponClass.DAGGER && source.getEntity() instanceof LivingEntity att && victim instanceof LivingEntity v) {
            Vec3 look = v.getLookAngle(); Vec3 toAtt = att.position().subtract(v.position()).normalize();
            if (look.dot(toAtt) < -0.4) return damage;
        }
        return 0;
    }

    @Override public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return;
        switch (cls) {
            case CLUB -> Util2.knock(target, 0.8, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            case HAMMER -> {
                if (attacker.fallDistance > 1.5) {
                    for (LivingEntity e : Util2.near(level, LivingEntity.class, target, 2.5, e -> e != attacker && e != target))
                        e.hurtServer(level, level.damageSources().mobAttack(attacker), 4);
                    level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
                }
                Util2.knock(target, 0.6, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            }
            case BATTLEAXE -> {
                for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack a = target.getItemBySlot(s);
                    if (!a.isEmpty() && a.isDamageableItem()) a.hurtAndBreak(3, target, s);
                }
                if (target instanceof Player p && p.isBlocking()) p.getCooldowns().addCooldown(p.getUseItem(), 100);
            }
            case TRUNCHEON -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), attacker);
            case SCYTHE -> {
                for (LivingEntity e : Util2.near(level, LivingEntity.class, attacker, 3.0, e -> e != attacker && e != target && e instanceof net.minecraft.world.entity.monster.Enemy))
                    e.hurtServer(level, level.damageSources().mobAttack(attacker), 3);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
            }
            default -> {}
        }
    }

    @Override public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (cls == WeaponClass.KATANA && slot == EquipmentSlot.MAINHAND && owner instanceof LivingEntity l && owner.tickCount % 20 == 0)
            l.addEffect(new MobEffectInstance(MobEffects.HASTE, 30, 0, true, false));
    }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (cls == WeaponClass.KNIFE) {
            if (level instanceof ServerLevel sl) {
                ThrownKnife.throwKnife(sl, player, stack.copyWithCount(1));
                if (!player.isCreative()) stack.shrink(1);
                sl.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.8F, 1.4F);
            }
            return InteractionResult.SUCCESS;
        }
        if (cls == WeaponClass.CLAYMORE && !player.getCooldowns().isOnCooldown(stack)) { player.startUsingItem(hand); return InteractionResult.CONSUME; }
        return InteractionResult.PASS;
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return cls == WeaponClass.CLAYMORE ? 72000 : 0; }
    @Override public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack s) { return cls == WeaponClass.CLAYMORE ? net.minecraft.world.item.ItemUseAnimation.SPEAR : net.minecraft.world.item.ItemUseAnimation.NONE; }

    @Override public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (cls != WeaponClass.CLAYMORE || !(level instanceof ServerLevel sl) || !(entity instanceof Player p)) return false;
        int charged = getUseDuration(stack, entity) - remaining;
        if (charged < 12) return false;
        SlashProjectile.slash(sl, p, 6F + Math.min(6F, charged / 8F));
        stack.hurtAndBreak(2, p, p.getUsedItemHand());
        p.getCooldowns().addCooldown(stack, 30);
        sl.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1F, 0.7F);
        return true;
    }
}
