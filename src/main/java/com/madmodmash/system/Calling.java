package com.madmodmash.system;

/**
 * Callings: the Mc-HG kit list reimagined as permanent RPG classes. One Calling at a time,
 * chosen with a Scroll of Calling (sold by the Traveling Artificer, rewarded by the Imperial Path).
 * Behaviour lives in {@link CallingEvents}.
 */
public enum Calling {
    LUMBERJACK, JACKHAMMER, FIREMAN, POSEIDON, WEREWOLF, FROSTY, WORM, CULTIVATOR, FLETCHER, NECRO, STOMPER, TURTLE,
    VAMPIRE, VIPER, SNAIL, KANGAROO, BOXER, BERSERKER, CANNIBAL, BEASTMASTER, MINER, MONSTER;

    public String key() {
        return "calling.madmodmash." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static Calling byName(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }
}
