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

/** A Mystery Mineral. Right-click it onto a weapon in your inventory to infuse its Trait. */
public class TraitCoreItem extends Item {
    public final Trait trait; public final int level;
    public TraitCoreItem(Properties p, Trait trait, int level) { super(p); this.trait = trait; this.level = level; }
    @Override public boolean overrideStackedOnOther(ItemStack self, net.minecraft.world.inventory.Slot slot, net.minecraft.world.inventory.ClickAction action, Player player) {
        if (action != net.minecraft.world.inventory.ClickAction.SECONDARY) return false;
        ItemStack target = slot.getItem();
        if (target.isEmpty() || !(target.has(DataComponents.WEAPON) || target.has(DataComponents.TOOL) || target.getItem() instanceof net.minecraft.world.item.BowItem)) return false;
        Trait.infuse(target, trait, level);
        self.shrink(1);
        player.playSound(SoundEvents.SMITHING_TABLE_USE, 1F, 1.2F);
        return true;
    }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        out.accept(Component.translatable("tooltip.madmodmash.core", Component.translatable("trait.madmodmash." + trait.name().toLowerCase(java.util.Locale.ROOT)),
                Component.translatable("enchantment.level." + level)).withStyle(trait.color));
        out.accept(Component.translatable("tooltip.madmodmash.core_help").withStyle(ChatFormatting.DARK_GRAY));
    }
}
