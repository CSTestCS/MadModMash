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

/** Return to where you last recalled from. */
public class ReturnTokenItem extends Item {
    public ReturnTokenItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        if (!ReturnTokenItem.returnPlayer(sp)) return InteractionResult.FAIL;
        player.getItemInHand(hand).shrink(1);
        return InteractionResult.SUCCESS;
    }
    public static boolean returnPlayer(ServerPlayer sp) {
        PlayerData d = ModAttachments.get(sp);
        if (d.returnPoint == null) { sp.sendSystemMessage(Component.translatable("msg.madmodmash.no_return").withStyle(ChatFormatting.RED)); return false; }
        ServerLevel target = sp.level().getServer().getLevel(d.returnPoint.dimension());
        if (target == null) return false;
        BlockPos p = d.returnPoint.pos();
        d.returnPoint = null;
        sp.teleportTo(target, p.getX() + 0.5, p.getY() + 0.1, p.getZ() + 0.5, java.util.Set.of(), sp.getYRot(), sp.getXRot(), false);
        target.playSound(null, p, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F);
        return true;
    }
}
