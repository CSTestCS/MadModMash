package com.madmodmash.system;

import com.madmodmash.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** One-off gifts when reaching higher ranks. */
public final class Unlocks {
    public static void grantRankPerks(ServerPlayer player, int rankIdx) {
        ItemStack gift = switch (rankIdx) {
            case 4 -> new ItemStack(ModItems.VILLAGE_IRON_CHESTPLATE.get());
            case 5 -> new ItemStack(ModItems.EMERALD_NEXUS_CORE.get());
            case 6 -> new ItemStack(ModItems.REQ_CARD.get(), 3);
            case 7 -> new ItemStack(ModItems.EMERALD_CHARM.get());
            case 8 -> new ItemStack(ModItems.IMPERIAL_CHARTER.get());
            case 9 -> new ItemStack(ModItems.HALO_OF_LIGHT.get());
            case 10 -> new ItemStack(ModItems.MASTER_KEY.get());
            case 11 -> new ItemStack(ModItems.NETHERITE_GILDAN.get());
            default -> ItemStack.EMPTY;
        };
        if (!gift.isEmpty()) {
            Util2.give(player, gift);
            player.sendSystemMessage(Component.translatable("msg.madmodmash.rank_gift", gift.getHoverName()).withStyle(ChatFormatting.GOLD));
        }
    }

    private Unlocks() {}
}
