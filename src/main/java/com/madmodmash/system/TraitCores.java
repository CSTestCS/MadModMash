package com.madmodmash.system;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/** Which Mystery Mineral carries which Trait at which strength (ingot = I, shard = II, gem = III). */
public final class TraitCores {
    private static final Map<Trait, String[]> CORES = new EnumMap<>(Trait.class);
    static {
        CORES.put(Trait.VOID, new String[]{"ostrum_ingot", "morium_shard", "liliota_gem"});
        CORES.put(Trait.AIR, new String[]{"calcius_ingot", "misket_shard", "limona_gem"});
        CORES.put(Trait.FIRE, new String[]{"lutum_ingot", "zawist_shard", "korkak_gem"});
        CORES.put(Trait.EARTH, new String[]{"brunius_ingot", "karamak_shard", "brazite_gem"});
        CORES.put(Trait.POISON, new String[]{"viridius_ingot", "vert_shard", "taz_gem"});
        CORES.put(Trait.STONE, new String[]{"tawnium_ingot", "tawnium_shard", "tawnium_gem"});
        CORES.put(Trait.ICE, new String[]{"carulem_ingot", "mavium_shard", "sapphire"});
        CORES.put(Trait.MIGHT, new String[]{"karite_ingot", "szarite_shard", "gri_gem"});
        CORES.put(Trait.LIGHTNING, new String[]{"flavum_ingot", "zoltite_shard", "sari_gem"});
        CORES.put(Trait.LIFE, new String[]{"rubrum_ingot", "krasny_shard", "ruby"});
        CORES.put(Trait.SHADOW, new String[]{"aterum_ingot", "aterum_shard", "siyah_gem"});
        CORES.put(Trait.LIGHT, new String[]{"albumin_ingot", "beyaz_shard", "biel_gem"});
    }

    public static ItemStack core(Trait trait, int level) {
        return Util2.stack("madmodmash:" + CORES.get(trait)[Math.max(1, Math.min(3, level)) - 1], 1);
    }

    private TraitCores() {}
}
