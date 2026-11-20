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

/** Halo-style armor abilities (Abilities.txt). */
public class AbilityModuleItem extends Item {
    public enum Kind { ACTIVE_CAMO, ARMOR_LOCK, DROP_SHIELD, JET_PACK, SPRINT }
    public final Kind kind;
    public AbilityModuleItem(Properties p, Kind kind) { super(p); this.kind = kind; }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (kind == Kind.JET_PACK) return InteractionResult.PASS;
        if (level instanceof ServerLevel sl) {
            switch (kind) {
                case ACTIVE_CAMO -> { player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0)); player.getCooldowns().addCooldown(s, 600); }
                case ARMOR_LOCK -> {
                    player.addEffect(new MobEffectInstance(ModEffects.ARMOR_LOCK, 80, 0));
                    player.getPersistentData().putLong("mm_emp_at", sl.getGameTime() + 80);
                    player.getCooldowns().addCooldown(s, 900);
                }
                case DROP_SHIELD -> {
                    var cloud = new net.minecraft.world.entity.AreaEffectCloud(sl, player.getX(), player.getY(), player.getZ());
                    cloud.setRadius(3.5F); cloud.setDuration(240); cloud.setRadiusPerTick(0); cloud.setWaitTime(0);
                    cloud.setOwner(player);
                    cloud.setCustomParticle(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF40B0E0));
                    cloud.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
                    cloud.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0));
                    sl.addFreshEntity(cloud);
                    player.getCooldowns().addCooldown(s, 1200);
                }
                case SPRINT -> { player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 2)); player.getCooldowns().addCooldown(s, 400); }
                default -> {}
            }
            s.hurtAndBreak(1, player, hand);
            sl.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.8F);
        }
        return InteractionResult.SUCCESS;
    }

    /** Every tick: jet pack thrust and armor-lock EMP release. */
    public static void tickJetpack(ServerPlayer p) {
        long emp = p.getPersistentData().getLongOr("mm_emp_at", 0L);
        ServerLevel sl = (ServerLevel) p.level();
        if (emp > 0 && sl.getGameTime() >= emp) {
            p.getPersistentData().remove("mm_emp_at");
            for (LivingEntity e : Util2.near(sl, LivingEntity.class, p, 5, e -> e != p)) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3), p);
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), p);
                Util2.knock(e, 1.0, p.getX() - e.getX(), p.getZ() - e.getZ());
            }
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 80, 2.5, 1, 2.5, 0.2);
            sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1F, 0.6F);
        }
        if (p.onGround() || p.isInWater() || !p.getLastClientInput().jump()) return;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.getItem() instanceof AbilityModuleItem m && m.kind == Kind.JET_PACK) {
                Vec3 v = p.getDeltaMovement();
                p.setDeltaMovement(v.x, Math.min(0.5, v.y + 0.12), v.z);
                p.syncVelocity = true; p.fallDistance = 0;
                if (p.tickCount % 10 == 0) s.hurtAndBreak(1, p, EquipmentSlot.MAINHAND);
                sl.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY(), p.getZ(), 2, 0.1, 0, 0.1, 0.01);
                return;
            }
        }
    }
}
