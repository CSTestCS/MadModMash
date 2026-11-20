package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Potions from HostileBuffs (Midas Touch), Quackthstone (Frozen, Void Fire, Health Penalty),
 *  Modjamo (Detect Ores) and the Zombie Plague mod. Brewing recipes are data-driven (data/madmodmash/recipe/brewing). */
public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, MadModMash.MODID);
    public static final DeferredHolder<Potion, Potion> MIDAS_TOUCH = POTIONS.register("midas_touch", () -> new Potion("midas_touch", new MobEffectInstance(ModEffects.MIDAS_TOUCH, 3600)));
    public static final DeferredHolder<Potion, Potion> FROZEN = POTIONS.register("frozen", () -> new Potion("frozen", new MobEffectInstance(ModEffects.FROZEN, 200)));
    public static final DeferredHolder<Potion, Potion> VOID_FIRE = POTIONS.register("void_fire", () -> new Potion("void_fire", new MobEffectInstance(ModEffects.VOID_FIRE, 300)));
    public static final DeferredHolder<Potion, Potion> HEALTH_PENALTY = POTIONS.register("health_penalty", () -> new Potion("health_penalty", new MobEffectInstance(ModEffects.HEALTH_PENALTY, 1200)));
    public static final DeferredHolder<Potion, Potion> DETECT_ORES = POTIONS.register("detect_ores", () -> new Potion("detect_ores", new MobEffectInstance(ModEffects.SEER_SIGHT, 1200)));
    public static final DeferredHolder<Potion, Potion> ZOMBIE_PLAGUE = POTIONS.register("zombie_plague", () -> new Potion("zombie_plague", new MobEffectInstance(ModEffects.ZOMBIE_PLAGUE, 2400)));
    public static final DeferredHolder<Potion, Potion> UNDEATH = POTIONS.register("undeath", () -> new Potion("undeath", new MobEffectInstance(ModEffects.UNDEATH, 3600)));

    private ModPotions() {}
}
