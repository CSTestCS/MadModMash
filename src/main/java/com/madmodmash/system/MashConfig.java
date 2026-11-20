package com.madmodmash.system;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MashConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue CHAMPION_CHANCE = B
            .comment("Base chance (0-1) for a hostile mob to spawn as a named Champion. Faction Wrath adds to this.")
            .defineInRange("championChance", 0.03, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue ALPHA_CHANCE = B
            .comment("Chance for a wild animal to spawn as an Alpha.")
            .defineInRange("alphaChance", 0.03, 0.0, 1.0);
    public static final ModConfigSpec.IntValue EVENT_INTERVAL = B
            .comment("Minutes between world-event rolls for each player.")
            .defineInRange("eventIntervalMinutes", 15, 1, 600);
    public static final ModConfigSpec.DoubleValue EVENT_CHANCE = B
            .comment("Base chance per roll that an event starts near a player (scaled up by Wrath).")
            .defineInRange("eventChance", 0.25, 0.0, 1.0);
    public static final ModConfigSpec.IntValue VISITOR_INTERVAL = B
            .comment("Ticks between bonfire visitor attempts.")
            .defineInRange("visitorIntervalTicks", 6000, 200, 240000);
    public static final ModConfigSpec.IntValue RENOWN_PER_PATH_LEVEL = B
            .comment("Renown required per level of the Imperial Path (100 levels).")
            .defineInRange("renownPerPathLevel", 150, 10, 10000);

    public static final ModConfigSpec SPEC = B.build();

    private MashConfig() {}
}
