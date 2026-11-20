package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameRules {
    public static final DeferredRegister<GameRule<?>> RULES = DeferredRegister.create(Registries.GAME_RULE, MadModMash.MODID);

    /** Dark Adventures: "Herobrine - essentially a virus that can only be removed by setting the gamerule to false". */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> HEROBRINE_HAUNTING = bool("herobrine_haunting", GameRuleCategory.MOBS, false);
    /** Dark Adventures: "Allow Mortis Infection Spread". */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> MORTIS_SPREAD = bool("mortis_spread", GameRuleCategory.MOBS, true);
    /** Dark Adventures: "Allow Common Mutations" - any champion may mutate regardless of wrath. */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> COMMON_MUTATIONS = bool("common_mutations", GameRuleCategory.MOBS, false);
    /** RPG "Allow Border Control": world border grows with the realm's total renown. */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> BORDER_CONTROL = bool("border_control", GameRuleCategory.MISC, false);
    /** Whether faction Wrath can spill over into public world events. */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> WORLD_EVENTS = bool("world_events", GameRuleCategory.MOBS, true);
    /** ModJam July '23 "Bad News". */
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> ENTRY_EVENTS = bool("entry_events", GameRuleCategory.PLAYER, true);

    private static DeferredHolder<GameRule<?>, GameRule<Boolean>> bool(String name, GameRuleCategory cat, boolean def) {
        return RULES.register(name, () -> new GameRule<>(cat, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean,
                Codec.BOOL, b -> b ? 1 : 0, def, FeatureFlagSet.of()));
    }

    private ModGameRules() {}
}
