package com.madmodmash.item;

import com.madmodmash.*;
import com.madmodmash.entity.*;
import com.madmodmash.registry.*;
import com.madmodmash.system.*;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Appraises Mystery Ores and Mystery Plants (CSA / RPG / Dark Adventures magnifiers). */
public class MagnifierItem extends Item {
    public final int tier;
    public MagnifierItem(Properties p, int tier) { super(p); this.tier = tier; }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        var state = level.getBlockState(pos);
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        boolean done = false;
        if (state.getBlock() instanceof com.madmodmash.block.MysteryOreBlock) { com.madmodmash.block.MysteryOreBlock.appraise(sl, pos, ctx.getPlayer(), tier); done = true; }
        else if (state.getBlock() instanceof com.madmodmash.block.MysteryPlantBlock) { com.madmodmash.block.MysteryPlantBlock.appraise(sl, pos, tier); done = true; }
        if (!done) return InteractionResult.PASS;
        boolean plant = state.getBlock() instanceof com.madmodmash.block.MysteryPlantBlock;
        if (!(tier == 5 && plant) && ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), ctx.getHand());
        sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1F, 1F);
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player instanceof ServerPlayer sp && target.getItemBySlot(EquipmentSlot.HEAD).isEmpty() == false) {
            ScannerItem.describe(sp, target);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
