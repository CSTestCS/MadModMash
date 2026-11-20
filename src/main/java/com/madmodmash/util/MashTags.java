package com.madmodmash.util;

import com.madmodmash.MadModMash;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class MashTags {
    public static final TagKey<EntityType<?>> CONSTRUCTS = TagKey.create(Registries.ENTITY_TYPE, MadModMash.id("constructs"));
    public static final TagKey<EntityType<?>> ILLAGER_HOST = TagKey.create(Registries.ENTITY_TYPE, MadModMash.id("faction/illager_host"));
    public static final TagKey<EntityType<?>> MORTIS_BLIGHT = TagKey.create(Registries.ENTITY_TYPE, MadModMash.id("faction/mortis_blight"));
    public static final TagKey<EntityType<?>> UNHOLY = TagKey.create(Registries.ENTITY_TYPE, MadModMash.id("faction/unholy"));
    public static final TagKey<EntityType<?>> WILDS = TagKey.create(Registries.ENTITY_TYPE, MadModMash.id("faction/wilds"));
    public static final TagKey<EntityType<?>> NETHER = TagKey.create(Registries.ENTITY_TYPE, MadModMash.id("faction/nether"));
    public static final TagKey<Item> GILDANS = TagKey.create(Registries.ITEM, MadModMash.id("gildans"));
    public static final TagKey<Item> CARVING_KNIVES = TagKey.create(Registries.ITEM, MadModMash.id("carving_knives"));
    public static final TagKey<Item> POWDERS = TagKey.create(Registries.ITEM, MadModMash.id("powders"));
    public static final TagKey<Block> CHISELABLE = TagKey.create(Registries.BLOCK, MadModMash.id("chiselable"));

    private MashTags() {}
}
