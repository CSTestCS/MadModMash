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

/** Quack / ModJam bedrock pickaxe: can pry bedrock loose (but never the world floor). */
public class BedrockPickaxeItem extends Item {
    public BedrockPickaxeItem(Properties p) { super(p); }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel(); BlockPos pos = ctx.getClickedPos();
        if (!level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEDROCK) || pos.getY() <= level.getMinY() + 1) return InteractionResult.PASS;
        if (level instanceof ServerLevel sl) {
            sl.removeBlock(pos, false);
            Util2.drop(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.BEDROCK));
            sl.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.6F, 0.6F);
            if (ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(25, ctx.getPlayer(), ctx.getHand());
        }
        return InteractionResult.SUCCESS;
    }
}
