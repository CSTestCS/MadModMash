package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModTiers {
    public static final ToolMaterial STEEL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 520, 6.5F, 2.5F, 12, repair("steel_tool_materials"));
    public static final ToolMaterial RUBY = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 900, 7.5F, 3.0F, 18, repair("ruby_tool_materials"));
    public static final ToolMaterial EMERALD = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 8.0F, 3.0F, 20, repair("emerald_tool_materials"));
    public static final ToolMaterial BEDROCK = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 8000, 7.0F, 4.0F, 5, repair("bedrock_tool_materials"));
    public static final ToolMaterial ASTROITE = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1400, 9.0F, 3.0F, 16, repair("astroite_tool_materials"));
    public static final ToolMaterial PLASTIC = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 180, 5.0F, 1.0F, 15, repair("plastic_tool_materials"));
    public static final ToolMaterial ANCIENT = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 30.0F, 3.0F, 10, repair("ancient_tool_materials"));

    private static TagKey<Item> repair(String name) {
        return TagKey.create(Registries.ITEM, MadModMash.id(name));
    }

    private ModTiers() {}
}
