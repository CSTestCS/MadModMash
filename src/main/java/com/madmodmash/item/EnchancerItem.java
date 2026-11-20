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

/** Battle-pass Enchancer: pushes one enchantment on your off-hand item past its limit (to X). */
public class EnchancerItem extends Item {
    public EnchancerItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(other);
        var ench = target.get(DataComponents.ENCHANTMENTS);
        if (ench == null || ench.isEmpty()) return InteractionResult.FAIL;
        if (!(level instanceof ServerLevel)) return InteractionResult.SUCCESS;
        net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> best = null; int lvl = 99;
        for (var e : ench.entrySet()) if (e.getIntValue() < 10 && e.getIntValue() < lvl) { best = e.getKey(); lvl = e.getIntValue(); }
        if (best == null) return InteractionResult.FAIL;
        final var fb = best; final int nl = lvl + 1;
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(target, m -> m.set(fb, nl));
        player.getItemInHand(hand).shrink(1);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1F, 1.5F);
        return InteractionResult.SUCCESS;
    }
}
