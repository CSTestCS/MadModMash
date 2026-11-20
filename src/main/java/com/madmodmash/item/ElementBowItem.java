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

/** Duckobrine fireball / lightning / freezing bows. */
public class ElementBowItem extends Item {
    public enum Kind { FIRE, LIGHTNING, ICE }
    public final Kind kind;
    public ElementBowItem(Properties p, Kind kind) { super(p); this.kind = kind; }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) { player.startUsingItem(hand); return InteractionResult.CONSUME; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack s) { return net.minecraft.world.item.ItemUseAnimation.BOW; }
    @Override public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!(level instanceof ServerLevel sl) || !(entity instanceof Player p)) return false;
        float power = Math.min(1F, (getUseDuration(stack, entity) - remaining) / 20F);
        if (power < 0.3F) return false;
        if (!p.isCreative()) {
            int slot = -1;
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (p.getInventory().getItem(i).is(Items.ARROW)) { slot = i; break; }
            if (slot < 0) return false;
            p.getInventory().getItem(slot).shrink(1);
        }
        MashProjectile.Kind k = switch (kind) { case FIRE -> MashProjectile.Kind.FIRE_CHARGE; case LIGHTNING -> MashProjectile.Kind.LIGHTNING_CHARGE; default -> MashProjectile.Kind.ICE_CHARGE; };
        MashProjectile.shoot(sl, p, ModEntities.BOW_CHARGE.get(), k, new ItemStack(kind == Kind.FIRE ? Items.FIRE_CHARGE : kind == Kind.ICE ? Items.SNOWBALL : Items.GLOWSTONE_DUST), 2.6F * power, 0.5F);
        sl.playSound(null, p.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1F, 0.8F);
        stack.hurtAndBreak(1, p, p.getUsedItemHand());
        return true;
    }
}
