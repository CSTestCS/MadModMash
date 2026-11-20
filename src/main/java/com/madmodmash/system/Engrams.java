package com.madmodmash.system;

import com.madmodmash.entity.LootEngram;
import com.madmodmash.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Loot Engrams (WildLoot): floating, glowing loot with 22 tiers ("_LootEngramValueOrder.txt").
 * Dropped by Champions, world events, advancements and crates. Strike or use one to decrypt it.
 */
public final class Engrams {
    public static final String[] TIERS = {"wood", "stone", "copper", "ice", "amethyst", "redstone", "honey", "iron", "anvil", "prismarine",
            "gold", "lapis", "emerald", "slime", "diamond", "obsidian", "crying_obsidian", "ancient_debris", "purpur", "netherite", "void", "bedrock"};
    public static final ChatFormatting[] COLORS = {ChatFormatting.DARK_GREEN, ChatFormatting.DARK_GRAY, ChatFormatting.GOLD, ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_RED, ChatFormatting.GOLD, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.DARK_AQUA,
            ChatFormatting.YELLOW, ChatFormatting.DARK_BLUE, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN, ChatFormatting.AQUA, ChatFormatting.BLACK,
            ChatFormatting.DARK_PURPLE, ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD, ChatFormatting.DARK_AQUA, ChatFormatting.BLACK};

    public static void spawn(ServerLevel level, Vec3 pos, int tier) {
        LootEngram e = ModEntities.LOOT_ENGRAM.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (e == null) return;
        e.setTier(Math.max(0, Math.min(TIERS.length - 1, tier)));
        e.snapTo(pos.x, pos.y + 1.0, pos.z);
        level.addFreshEntity(e);
    }

    private Engrams() {}
}
