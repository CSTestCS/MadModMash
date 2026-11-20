package com.madmodmash.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.GlobalPos;

/** Everything the mod remembers about a player. Survives death (copyOnDeath). */
public final class PlayerData {
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("renown", 0).forGetter(d -> d.renown),
            Codec.INT.optionalFieldOf("path_claimed", 0).forGetter(d -> d.pathClaimed),
            Codec.BOOL.optionalFieldOf("charter", false).forGetter(d -> d.charter),
            Codec.STRING.optionalFieldOf("calling", "").forGetter(d -> d.calling),
            Codec.LONG.optionalFieldOf("calling_since", 0L).forGetter(d -> d.callingSince),
            Codec.BOOL.optionalFieldOf("entry_done", false).forGetter(d -> d.entryDone),
            GlobalPos.CODEC.optionalFieldOf("recall").forGetter(d -> Optional.ofNullable(d.recall)),
            GlobalPos.CODEC.optionalFieldOf("return_point").forGetter(d -> Optional.ofNullable(d.returnPoint)),
            Codec.LONG.optionalFieldOf("bank", 0L).forGetter(d -> d.bank),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("counters", Map.of()).forGetter(d -> d.counters),
            Codec.STRING.listOf().optionalFieldOf("tamed", List.of()).forGetter(d -> new ArrayList<>(d.tamed))
    ).apply(i, PlayerData::new));

    public int renown;
    public int pathClaimed;
    public boolean charter;
    public String calling;
    public long callingSince;
    public boolean entryDone;
    public GlobalPos recall;
    public GlobalPos returnPoint;
    public long bank;
    public final Map<String, Integer> counters;
    public final Set<String> tamed;
    // transient
    public int shieldCooldown;
    public boolean jumpHeld;
    public boolean doubleJumped;
    public int moduleCooldown;

    public PlayerData() {
        this(0, 0, false, "", 0L, false, Optional.empty(), Optional.empty(), 0L, Map.of(), List.of());
    }

    private PlayerData(int renown, int pathClaimed, boolean charter, String calling, long callingSince, boolean entryDone,
                       Optional<GlobalPos> recall, Optional<GlobalPos> returnPoint, long bank, Map<String, Integer> counters, List<String> tamed) {
        this.renown = renown;
        this.pathClaimed = pathClaimed;
        this.charter = charter;
        this.calling = calling;
        this.callingSince = callingSince;
        this.entryDone = entryDone;
        this.recall = recall.orElse(null);
        this.returnPoint = returnPoint.orElse(null);
        this.bank = bank;
        this.counters = new HashMap<>(counters);
        this.tamed = new LinkedHashSet<>(tamed);
    }

    public int counter(String key) {
        return counters.getOrDefault(key, 0);
    }

    public int add(String key, int amount) {
        int v = counter(key) + amount;
        counters.put(key, v);
        return v;
    }
}
