package com.madmodmash.system;

import com.madmodmash.util.MashTags;
import net.minecraft.ChatFormatting;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

/** The four (five) threats to the Emerald Empire. */
public enum Faction {
    ILLAGER_HOST("The Illager Host", ChatFormatting.DARK_GRAY, MashTags.ILLAGER_HOST),
    MORTIS_BLIGHT("The Mortis Blight", ChatFormatting.DARK_GREEN, MashTags.MORTIS_BLIGHT),
    UNHOLY("The Herobrine Cult", ChatFormatting.DARK_PURPLE, MashTags.UNHOLY),
    NETHER("The Nether Legion", ChatFormatting.RED, MashTags.NETHER),
    WILDS("The Restless Wilds", ChatFormatting.GREEN, MashTags.WILDS);

    public final String title;
    public final ChatFormatting color;
    public final TagKey<EntityType<?>> tag;

    Faction(String title, ChatFormatting color, TagKey<EntityType<?>> tag) {
        this.title = title;
        this.color = color;
        this.tag = tag;
    }

    public static Faction of(LivingEntity e) {
        for (Faction f : values()) if (e.is(f.tag)) return f;
        if (e.is(EntityTypeTags.UNDEAD)) return MORTIS_BLIGHT;
        if (e.is(EntityTypeTags.ILLAGER)) return ILLAGER_HOST;
        if (e instanceof Enemy) return WILDS;
        return null;
    }
}
