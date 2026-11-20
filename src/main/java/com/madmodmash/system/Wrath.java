package com.madmodmash.system;

import com.madmodmash.MadModMash;
import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Faction Wrath (HostileBuffs "CountDeaths": "The undead grow stronger!").
 * Killing a faction's creatures raises its Wrath. Wrath raises Champion odds, mutation odds and world-event
 * odds for that faction. Completing an event against a faction humbles it again.
 */
public final class Wrath extends SavedData {
    public static final Codec<Wrath> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(Wrath::new, Wrath::save);
    public static final SavedDataType<Wrath> TYPE = new SavedDataType<>(MadModMash.id("wrath"), Wrath::new, CODEC);
    public static final int PER_TIER = 100, MAX = 1000;

    private final EnumMap<Faction, Integer> wrath = new EnumMap<>(Faction.class);

    public Wrath() {}

    private Wrath(Map<String, Integer> map) {
        map.forEach((k, v) -> {
            try { wrath.put(Faction.valueOf(k), v); } catch (IllegalArgumentException ignored) {}
        });
    }

    private Map<String, Integer> save() {
        Map<String, Integer> m = new HashMap<>();
        wrath.forEach((k, v) -> m.put(k.name(), v));
        return m;
    }

    public static Wrath get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static Wrath get(ServerLevel level) {
        return get(level.getServer());
    }

    public int value(Faction f) {
        return wrath.getOrDefault(f, 0);
    }

    public int tier(Faction f) {
        return Math.min(10, value(f) / PER_TIER);
    }

    public Faction angriest() {
        Faction best = null; int v = -1;
        for (Faction f : Faction.values()) if (value(f) > v) { v = value(f); best = f; }
        return best;
    }

    public void add(MinecraftServer server, Faction f, int amount) {
        int before = value(f);
        int after = Math.max(0, Math.min(MAX, before + amount));
        wrath.put(f, after);
        setDirty();
        if (after / PER_TIER > before / PER_TIER) {
            Component title = Component.translatable("msg.madmodmash.wrath_rises." + f.name().toLowerCase(java.util.Locale.ROOT)).withStyle(f.color, ChatFormatting.BOLD);
            for (var p : server.getPlayerList().getPlayers()) {
                p.connection.send(new ClientboundSetTitleTextPacket(title));
            }
        }
    }

    /** Slow natural cooling, called once a minute. */
    public void cool() {
        for (Faction f : Faction.values()) if (value(f) > 0) wrath.put(f, value(f) - 1);
        setDirty();
    }
}
