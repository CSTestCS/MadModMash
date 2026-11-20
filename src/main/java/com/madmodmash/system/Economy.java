package com.madmodmash.system;

import com.madmodmash.item.CoinPouchItem;
import com.madmodmash.item.GildanItem;
import com.madmodmash.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Gildans: one currency for the whole mod. Values are in copper gildans; each tier is worth nine of the last. */
public final class Economy {
    public static final List<Supplier<Item>> TIERS = List.of(
            () -> ModItems.NETHERITE_GILDAN.get(), () -> ModItems.DIAMOND_GILDAN.get(), () -> ModItems.AMETHYST_GILDAN.get(),
            () -> ModItems.EMERALD_GILDAN.get(), () -> ModItems.GOLD_GILDAN.get(), () -> ModItems.SILVER_GILDAN.get(), () -> ModItems.COPPER_GILDAN.get());

    public static long valueOf(ItemStack s) {
        return s.getItem() instanceof GildanItem g ? g.value * s.getCount() : 0;
    }

    /** Break a copper value into the fewest coins. */
    public static void give(Player player, long copper) {
        for (Supplier<Item> t : TIERS) {
            long v = ((GildanItem) t.get()).value;
            long n = copper / v;
            copper -= n * v;
            while (n > 0) {
                int c = (int) Math.min(64, n);
                Util2.give(player, new ItemStack(t.get(), c));
                n -= c;
            }
        }
    }

    public static void drop(ServerLevel level, double x, double y, double z, long copper) {
        for (Supplier<Item> t : TIERS) {
            long v = ((GildanItem) t.get()).value;
            long n = copper / v;
            copper -= n * v;
            if (n > 0) Util2.drop(level, x, y, z, new ItemStack(t.get(), (int) Math.min(64, n)));
        }
    }

    /** Total wealth carried: loose coins plus pouches. */
    public static long wealth(Player player) {
        long total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            total += valueOf(s);
            if (s.getItem() instanceof CoinPouchItem) total += CoinPouchItem.coins(s);
        }
        return total;
    }

    /** Pay a price from pouches first then loose coins, returning change. */
    public static boolean pay(Player player, long price) {
        if (wealth(player) < price) return false;
        long remaining = price;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof CoinPouchItem) {
                long have = CoinPouchItem.coins(s);
                long take = Math.min(have, remaining);
                CoinPouchItem.setCoins(s, have - take);
                remaining -= take;
            }
        }
        long paidLoose = 0;
        for (int i = 0; i < inv.getContainerSize() && paidLoose < remaining; i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof GildanItem) {
                paidLoose += valueOf(s);
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (paidLoose > remaining) give(player, paidLoose - remaining);
        return true;
    }

    private Economy() {}
}
