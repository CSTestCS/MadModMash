package com.madmodmash.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** Placement rules for naturally spawning creatures. Biomes are set by biome modifiers (data/madmodmash/neoforge/biome_modifier). */
public final class ModSpawns {
    public static void registerPlacements(RegisterSpawnPlacementsEvent e) {
        for (var slime : new EntityType[]{ModEntities.STONE_SLIME.get(), ModEntities.SAND_SLIME.get(), ModEntities.WOOD_SLIME.get(), ModEntities.EARTH_SLIME.get(),
                ModEntities.COAL_SLIME.get(), ModEntities.IRON_SLIME.get(), ModEntities.GOLD_SLIME.get(), ModEntities.REDSTONE_SLIME.get(), ModEntities.LAPIS_SLIME.get(),
                ModEntities.DIAMOND_SLIME.get(), ModEntities.EMERALD_SLIME.get(), ModEntities.OBSIDIAN_SLIME.get(), ModEntities.ICE_SLIME.get(), ModEntities.LAVA_SLIME.get(),
                ModEntities.HONEY_SLIME.get(), ModEntities.GLASS_SLIME.get(), ModEntities.GLOWSTONE_SLIME.get(), ModEntities.NETHERRACK_SLIME.get(),
                ModEntities.SOUL_SLIME.get(), ModEntities.PRISMARINE_SLIME.get(), ModEntities.NETHERITE_SLIME.get()}) {
            monster(e, slime);
        }
        animal(e, ModEntities.MONKEY.get());
        animal(e, ModEntities.RAM.get());
        animal(e, ModEntities.BOAR.get());
        animal(e, ModEntities.GOLDEN_EAGLE.get());
        monster(e, ModEntities.ZEPHYR.get());
        monster(e, ModEntities.LOCUST.get());
        monster(e, ModEntities.IRON_MIMIC.get());
        monster(e, ModEntities.SUMMONER.get());
        monster(e, ModEntities.MORTIS_ZOMBIE.get());
        mob(e, ModEntities.ILLAGER_BUTCHER.get());
        mob(e, ModEntities.ILLAGER_FARMER.get());
        mob(e, ModEntities.ILLAGER_SMITH.get());
        mob(e, ModEntities.ILLAGER_PRIEST.get());
        mob(e, ModEntities.ILLAGER_LIBRARIAN.get());
        mob(e, ModEntities.ILLAGER_NITWIT.get());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void monster(RegisterSpawnPlacementsEvent e, EntityType type) {
        e.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, level, reason, pos, rand) -> Monster.checkAnyLightMonsterSpawnRules((EntityType) t, level, reason, pos, rand), RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void animal(RegisterSpawnPlacementsEvent e, EntityType type) {
        e.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, level, reason, pos, rand) -> Animal.checkAnimalSpawnRules((EntityType) t, level, reason, pos, rand), RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void mob(RegisterSpawnPlacementsEvent e, EntityType type) {
        e.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, level, reason, pos, rand) -> Mob.checkMobSpawnRules((EntityType) t, level, reason, pos, rand), RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private ModSpawns() {}
}
