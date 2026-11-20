package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MadModMash.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSAULT_RIFLE_FIRE = reg("assault_rifle_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> BATTLE_RIFLE_FIRE = reg("battle_rifle_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAM_RIFLE_FIRE = reg("beam_rifle_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLE_EQUIP = reg("rifle_equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> SAD_SONG = reg("sad_song");

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(MadModMash.id(name)));
    }

    private ModSounds() {}
}
