package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import com.madmodmash.system.MashEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MadModMash.MODID);

    /** Bleeding from a carving knife or spike mat (Dark Adventures "Carving"). */
    public static final DeferredHolder<MobEffect, MobEffect> CARVING = EFFECTS.register("carving",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x9a1010, 30, MashEffect::bleed));
    /** Quackthstone "Frozen": rooted in place. */
    public static final DeferredHolder<MobEffect, MobEffect> FROZEN = EFFECTS.register("frozen",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x9ad8ff, 1, MashEffect::frozen)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, MadModMash.id("frozen"), -0.9, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH, MadModMash.id("frozen"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    /** Quackthstone "Void Fire": ender flame that burns and makes you blink. */
    public static final DeferredHolder<MobEffect, MobEffect> VOID_FIRE = EFFECTS.register("void_fire",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x5a20a0, 20, MashEffect::voidFire));
    /** Quackthstone "Health Penalty": lowered max health. */
    public static final DeferredHolder<MobEffect, MobEffect> HEALTH_PENALTY = EFFECTS.register("health_penalty",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x5a0000, 0, null)
                    .addAttributeModifier(Attributes.MAX_HEALTH, MadModMash.id("health_penalty"), -4.0, AttributeModifier.Operation.ADD_VALUE));
    /** HostileBuffs "Midas Touch": kills drop gildans. */
    public static final DeferredHolder<MobEffect, MobEffect> MIDAS_TOUCH = EFFECTS.register("midas_touch",
            () -> new MashEffect(MobEffectCategory.BENEFICIAL, 0xf2c02a, 0, null));
    /** Zombie Plague: die while infected and you rise as Mortis. */
    public static final DeferredHolder<MobEffect, MobEffect> ZOMBIE_PLAGUE = EFFECTS.register("zombie_plague",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x557a2f, 60, MashEffect::plague));
    /** Modjamo "Detect Ores": reveals ores through the stone. */
    public static final DeferredHolder<MobEffect, MobEffect> SEER_SIGHT = EFFECTS.register("seer_sight",
            () -> new MashEffect(MobEffectCategory.BENEFICIAL, 0x40e0e0, 40, MashEffect::seerSight));
    /** RPG "Undeath": the undead mistake you for one of their own. */
    public static final DeferredHolder<MobEffect, MobEffect> UNDEATH = EFFECTS.register("undeath",
            () -> new MashEffect(MobEffectCategory.NEUTRAL, 0x3a3a3a, 0, null));
    /** Armor Lock ability: invulnerable but rooted. */
    public static final DeferredHolder<MobEffect, MobEffect> ARMOR_LOCK = EFFECTS.register("armor_lock",
            () -> new MashEffect(MobEffectCategory.BENEFICIAL, 0xc0c040, 0, null)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, MadModMash.id("armor_lock"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    /** Bonfire "Stasis" for hostiles. */
    public static final DeferredHolder<MobEffect, MobEffect> STASIS = EFFECTS.register("stasis",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x8a3aa0, 0, null)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, MadModMash.id("stasis"), -0.75, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    /** Subdued by a truncheon: will not fight back, can be led to a bounty buyer. */
    public static final DeferredHolder<MobEffect, MobEffect> SUBDUED = EFFECTS.register("subdued",
            () -> new MashEffect(MobEffectCategory.HARMFUL, 0x7a7a7a, 0, null)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, MadModMash.id("subdued"), -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE, MadModMash.id("subdued"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    /** Bonfire flight (Volare powder). */
    public static final DeferredHolder<MobEffect, MobEffect> BONFIRE_FLIGHT = EFFECTS.register("bonfire_flight",
            () -> new MashEffect(MobEffectCategory.BENEFICIAL, 0x7fffff, 0, null));

    private ModEffects() {}
}
