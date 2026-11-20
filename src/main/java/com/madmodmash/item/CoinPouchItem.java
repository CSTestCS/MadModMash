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

/** Collects gildans automatically. Use: withdraw everything as coins. Sneak-use: deposit loose coins. */
public class CoinPouchItem extends Item {
    public CoinPouchItem(Properties p) { super(p); }
    public static long coins(ItemStack s) { CustomData cd = s.get(DataComponents.CUSTOM_DATA); return cd == null ? 0 : cd.copyTag().getLongOr("mm_coins", 0L); }
    public static void setCoins(ItemStack s, long v) { CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putLong("mm_coins", v)); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (level instanceof ServerLevel) {
            if (player.isShiftKeyDown()) {
                long total = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack s = player.getInventory().getItem(i);
                    if (s.getItem() instanceof GildanItem) { total += Economy.valueOf(s); player.getInventory().setItem(i, ItemStack.EMPTY); }
                }
                setCoins(pouch, coins(pouch) + total);
            } else { long c = coins(pouch); setCoins(pouch, 0); Economy.give(player, c); }
            level.playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1F, 1F);
        }
        return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        out.accept(Component.translatable("tooltip.madmodmash.coins", coins(s)).withStyle(ChatFormatting.GOLD));
    }
}
