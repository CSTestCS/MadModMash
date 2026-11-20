package com.madmodmash.registry;

import net.minecraft.world.food.FoodProperties;

public final class Foods {
    public static final FoodProperties ALPHA_BEEF = new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties ALPHA_STEAK = new FoodProperties.Builder().nutrition(12).saturationModifier(1.0F).build();
    public static final FoodProperties BANANA = new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build();
    public static final FoodProperties SODA = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).alwaysEdible().build();

    private Foods() {}
}
