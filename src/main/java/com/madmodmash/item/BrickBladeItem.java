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

/** The Lego sword, as a builder's blade: snaps toy bricks into place. */
public class BrickBladeItem extends Item {
    public BrickBladeItem(Properties p) { super(p); }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!level.getBlockState(at).canBeReplaced()) return InteractionResult.FAIL;
        if (level instanceof ServerLevel) {
            var bricks = ModItems.TAB_CONTENTS.get("bricks");
            var item = bricks.get(level.getRandom().nextInt(bricks.size())).get();
            if (item instanceof net.minecraft.world.item.BlockItem bi) level.setBlockAndUpdate(at, bi.getBlock().defaultBlockState());
            level.playSound(null, at, SoundEvents.BAMBOO_WOOD_PLACE, SoundSource.BLOCKS, 1F, 1.5F);
            if (ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), ctx.getHand());
        }
        return InteractionResult.SUCCESS;
    }
}
