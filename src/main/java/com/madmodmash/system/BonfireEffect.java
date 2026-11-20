package com.madmodmash.system;

/**
 * The "Bonfire Effects" list from the Dark Adventures notes. Throw a powder into a lit bonfire
 * and it radiates this effect. Colours drive the flame particles.
 */
public enum BonfireEffect {
    NONE(0xff9a20), STRENGTH(0xd02020), SPEED(0x40e0e0), RESISTANCE(0xf2c02a), REGENERATION(0xff70a0),
    WATER_BREATHING(0x3070e0), LEAPING(0x50c040), GLOWING(0xffffff), INVISIBILITY(0x9a9a9a), HEROISM(0x2ad06a),
    NIGHT_VISION(0x1a2a8a), LUCK(0x1a6a2a), FLIGHT(0x7fffff), FIRE_RESISTANCE(0xff8020), REPULSION(0x202020),
    COMMAND_ANIMALS(0xa0c040), PROTECTIVE_LIGHT(0xf8f0a0), STASIS(0x8a3aa0), STORM(0x5a2a8a), CLEAR_WEATHER(0x8ad0ff),
    SUMMON_VISITOR(0xc070ff), CLEANSING(0xe0e0e0), POSSESSION(0x40d0e0), GRAVITY(0xc0c0c0), SMITING(0x6a40c0),
    GROWTH(0x90f090), POISON(0x9adf40), ELEMENTALISM(0xffffff), DISSOLUTION(0x101010);

    public final int color;

    BonfireEffect(int color) {
        this.color = color;
    }

    public String translationKey() {
        return "bonfire.madmodmash." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
