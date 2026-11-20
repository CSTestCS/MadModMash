package com.madmodmash.system;

import com.madmodmash.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The Imperial Path: the "__BattlePass.txt" free/paid tracks as a 100-level reward ladder fed by Renown.
 * The paid track became the Emperor's Track, unlocked in-game with an Imperial Charter.
 * Entries are "namespace:item count". Trims map onto vanilla smithing templates; Transmog Tickets became gildans.
 */
public final class ImperialPath {
    private static final String M = "madmodmash:", V = "minecraft:";
    static final String[] FREE = {
            V + "stick 16", M + "tattered_chestplate 1", V + "bread 16", M + "wooden_club 1", V + "oak_log 16",
            M + "wooden_knife 1", V + "bread 32", M + "wooden_hammer 1", M + "silver_gildan 2", M + "stone_chisel 1",
            V + "arrow 16", M + "wooden_battleaxe 1", V + "oak_log 32", M + "sandstone_chisel 1", V + "bread 32",
            V + "wayfinder_armor_trim_smithing_template 1", V + "torch 64", M + "stone_club 1", M + "silver_gildan 3", M + "diorite_chisel 1",
            V + "chicken 16", M + "stone_knife 1", V + "coal 16", M + "granite_chisel 1", V + "leather 16",
            M + "stone_hammer 1", V + "cooked_chicken 16", M + "andesite_chisel 1", M + "silver_gildan 4", M + "stone_battleaxe 1",
            V + "oak_log 64", V + "dune_armor_trim_smithing_template 1", V + "saddle 1", M + "golden_truncheon 1", V + "cooked_beef 16",
            M + "fabric 8", V + "pig_spawn_egg 4", M + "golden_knife 1", M + "silver_gildan 5", M + "copper_magnifier 1",
            V + "experience_bottle 16", M + "golden_hammer 1", V + "lapis_lazuli 16", V + "host_armor_trim_smithing_template 1", V + "cooked_beef 32",
            M + "golden_battleaxe 1", V + "stone 64", V + "wayfinder_armor_trim_smithing_template 1", M + "gold_gildan 1", M + "wizard_chestplate 1",
            V + "redstone 64", M + "redstone_core 1", V + "slime_ball 16", M + "iron_truncheon 1", V + "cooked_beef 64",
            V + "shaper_armor_trim_smithing_template 1", V + "oak_log 64", M + "iron_knife 1", M + "gold_gildan 2", V + "wild_armor_trim_smithing_template 1",
            V + "tuff 64", M + "iron_hammer 1", M + "chainmail_sheet 16", M + "iron_magnifier 1", V + "wolf_spawn_egg 1",
            M + "iron_battleaxe 1", M + "enchancer 1", V + "eye_armor_trim_smithing_template 1", M + "gold_gildan 2", V + "sentry_armor_trim_smithing_template 1",
            V + "totem_of_undying 1", M + "fabric 16", M + "enchancer 1", M + "diamond_club 1", V + "golden_carrot 64",
            M + "mystery_chestplate 1", V + "experience_bottle 32", M + "diamond_knife 1", M + "emerald_gildan 1", M + "diamond_chisel 1",
            V + "echo_shard 8", M + "diamond_hammer 1", M + "warden_soul 1", V + "coast_armor_trim_smithing_template 1", V + "phantom_membrane 16",
            M + "diamond_battleaxe 1", V + "experience_bottle 64", M + "obsidian_chisel 1", M + "emerald_gildan 2", M + "netherite_truncheon 1",
            M + "enchancer 1", V + "bolt_armor_trim_smithing_template 1", V + "endermite_spawn_egg 4", M + "netherite_knife 1", V + "golden_carrot 64",
            M + "netherite_carving_knife 1", V + "silence_armor_trim_smithing_template 1", M + "netherite_hammer 1", M + "netherite_battleaxe 1", M + "cursed_pumpkin 1"
    };
    static final String[] EMPEROR = {
            M + "silver_gildan 4", M + "stone_club 1", V + "coast_armor_trim_smithing_template 1", V + "wooden_spear 1", M + "magic_card_deck 1",
            M + "enchancer 1", M + "wooden_scythe 1", V + "item_frame 8", M + "silver_gildan 4", M + "pocketball 1",
            V + "wild_armor_trim_smithing_template 1", V + "saddle 1", M + "wooden_katana 1", M + "enchancer 1", M + "wolfpelt_chestplate 1",
            M + "calcite_chisel 1", V + "wayfinder_armor_trim_smithing_template 1", V + "stone_spear 1", M + "gold_gildan 1", M + "pocketball 4",
            V + "raiser_armor_trim_smithing_template 1", V + "leather 16", M + "stone_scythe 1", M + "enchancer 2", V + "rib_armor_trim_smithing_template 1",
            M + "ghillie_helmet 1", V + "snout_armor_trim_smithing_template 1", V + "ender_pearl 4", M + "gold_gildan 1", M + "tuff_chisel 1",
            M + "stone_katana 1", M + "pocketball 8", V + "dune_armor_trim_smithing_template 1", M + "enchancer 2", V + "golden_spear 1",
            M + "fabric 32", V + "spawner 1", M + "gold_gildan 2", M + "netherrack_chisel 1", M + "golden_scythe 1",
            V + "item_frame 16", V + "snout_armor_trim_smithing_template 1", M + "enchancer 4", V + "host_armor_trim_smithing_template 1", M + "basalt_chisel 1",
            M + "golden_katana 1", V + "ender_pearl 8", M + "gold_gildan 4", M + "pocketball 16", V + "shaper_armor_trim_smithing_template 1",
            M + "blackstone_chisel 1", M + "wizard_helmet 1", M + "enchancer 4", V + "vex_armor_trim_smithing_template 1", V + "iron_spear 1",
            V + "shaper_armor_trim_smithing_template 1", V + "spawner 1", M + "emerald_gildan 1", V + "ender_pearl 16", M + "iron_scythe 1",
            M + "quartz_chisel 1", V + "wild_armor_trim_smithing_template 1", M + "enchancer 4", V + "sentry_armor_trim_smithing_template 1", M + "pocketball 32",
            M + "iron_katana 1", M + "throwing_star 32", M + "emerald_gildan 2", V + "item_frame 32", V + "eye_armor_trim_smithing_template 1",
            M + "end_stone_chisel 1", V + "flow_armor_trim_smithing_template 1", M + "enchancer 8", M + "master_key 1", V + "diamond_spear 1",
            V + "silence_armor_trim_smithing_template 1", M + "fireball_bow 1", M + "emerald_gildan 2", V + "ender_pearl 16", M + "diamond_scythe 1",
            M + "pocketball 64", V + "ward_armor_trim_smithing_template 1", M + "enchancer 8", M + "lightning_bow 1", M + "emerald_gildan 3",
            V + "netherite_spear 1", M + "lock_pick 16", M + "enchancer 8", M + "netherite_scythe 1", V + "spawner 4",
            M + "netherite_scythe 1", V + "bedrock 64", M + "wizard_helmet 1", V + "end_portal_frame 12", M + "netherite_katana 1", M + "halo_of_fire 1"
    };

    public static int level(int renown) {
        return Math.min(100, renown / MashConfig.RENOWN_PER_PATH_LEVEL.get());
    }

    public static void claimPending(ServerPlayer player) {
        PlayerData d = ModAttachments.get(player);
        int target = level(d.renown);
        while (d.pathClaimed < target) {
            int idx = d.pathClaimed;
            d.pathClaimed++;
            grant(player, FREE[Math.min(idx, FREE.length - 1)]);
            if (d.charter) grant(player, EMPEROR[Math.min(idx, EMPEROR.length - 1)]);
            player.sendSystemMessage(Component.translatable("msg.madmodmash.path_level", d.pathClaimed).withStyle(ChatFormatting.GOLD));
        }
    }

    /** Called when a Charter is used: retroactively grant the Emperor's Track up to the current level. */
    public static void grantCharterBacklog(ServerPlayer player) {
        PlayerData d = ModAttachments.get(player);
        for (int i = 0; i < d.pathClaimed; i++) grant(player, EMPEROR[Math.min(i, EMPEROR.length - 1)]);
    }

    private static void grant(ServerPlayer player, String entry) {
        String[] parts = entry.split(" ");
        ItemStack s = Util2.stack(parts[0], Integer.parseInt(parts[1]));
        if (!s.isEmpty()) Util2.give(player, s);
    }

    public static String preview(int lvl, boolean emperor) {
        String[] arr = emperor ? EMPEROR : FREE;
        return lvl >= 0 && lvl < arr.length ? arr[lvl] : "";
    }

    private ImperialPath() {}
}
