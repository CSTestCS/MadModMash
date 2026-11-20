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

/** Duckobrine enchanted flint & steel: lights fires, and the Herobrine shrine. */
public class EnchantedFlintItem extends Item {
    public EnchantedFlintItem(Properties p) { super(p); }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel(); BlockPos pos = ctx.getClickedPos();
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        var B = net.minecraft.world.level.block.Blocks.class;
        if (sl.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.NETHERRACK) && sl.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK)) {
            int torches = 0;
            for (Direction d : Direction.Plane.HORIZONTAL) if (sl.getBlockState(pos.relative(d)).is(net.minecraft.world.level.block.Blocks.REDSTONE_TORCH)) torches++;
            if (torches >= 2) {
                sl.setBlockAndUpdate(pos.above(), net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
                LightningBoltHelper.strike(sl, pos.above());
                Util2.spawn(sl, ModEntities.HEROBRINE.get(), Util2.surface(sl, pos, 4, 8));
                sl.getGameRules().set(ModGameRules.HEROBRINE_HAUNTING.get(), true, sl.getServer());
                if (ctx.getPlayer() != null) ctx.getPlayer().sendSystemMessage(Component.translatable("msg.madmodmash.not_alone").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
                ctx.getItemInHand().hurtAndBreak(8, ctx.getPlayer(), ctx.getHand());
                return InteractionResult.SUCCESS;
            }
        }
        BlockPos at = pos.relative(ctx.getClickedFace());
        if (sl.getBlockState(at).isAir()) {
            sl.setBlockAndUpdate(at, net.minecraft.world.level.block.BaseFireBlock.getState(sl, at));
            sl.playSound(null, at, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1F, 0.6F);
            if (ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), ctx.getHand());
        }
        return InteractionResult.SUCCESS;
    }
}
