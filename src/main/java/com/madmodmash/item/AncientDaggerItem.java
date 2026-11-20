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

/** Ancient Dagger: cuts through stone like butter and turns blocks to stone. */
public class AncientDaggerItem extends Item {
    public AncientDaggerItem(Properties p) { super(p); }
    @Override public float getDestroySpeed(ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE) ? 30F : super.getDestroySpeed(stack, state);
    }
    @Override public boolean isCorrectToolForDrops(ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE) || super.isCorrectToolForDrops(stack, state);
    }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel(); BlockPos pos = ctx.getClickedPos();
        var st = level.getBlockState(pos);
        if (st.getDestroySpeed(level, pos) < 0 || level.getBlockEntity(pos) != null || st.is(net.minecraft.world.level.block.Blocks.STONE)) return InteractionResult.PASS;
        if (level instanceof ServerLevel) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1F, 0.8F);
            if (ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), ctx.getHand());
        }
        return InteractionResult.SUCCESS;
    }
}
