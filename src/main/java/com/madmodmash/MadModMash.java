package com.madmodmash;

import com.madmodmash.registry.*;
import com.madmodmash.system.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * MadModMash: The Emerald Empire.
 *
 * <p>Everything in the mod hangs off a handful of shared systems so that content from
 * twenty-seven very different source mods feeds into one loop:
 * <ul>
 *   <li>{@link Renown} - Imperial rank and the 100-level Imperial Path (the battle pass notes).</li>
 *   <li>{@link Wrath} - four hostile factions grow stronger as you fight them (HostileBuffs) and
 *       spill over into {@link WorldEvents} (Dark Adventures public events).</li>
 *   <li>{@link Champions} - named, mutated elites that carry Trait cores, trophies and bounties.</li>
 *   <li>{@link MobMaterials} - every creature yields materials (MobMats notes) feeding armor and weapons.</li>
 *   <li>Gildans - one currency used by contracts, bounties, merchants, crates, vending machines.</li>
 * </ul>
 */
@Mod(MadModMash.MODID)
public class MadModMash {
    public static final String MODID = "madmodmash";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public MadModMash(IEventBus modBus, ModContainer container) {
        ModSounds.SOUNDS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModTabs.TABS.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        ModGameRules.RULES.register(modBus);
        ModVillagers.POI_TYPES.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModPotions.POTIONS.register(modBus);

        modBus.addListener(ModEntities::attributes);
        modBus.addListener(ModSpawns::registerPlacements);
        modBus.addListener(ModTabs::addToVanillaTabs);
        container.registerConfig(ModConfig.Type.SYNCED, MashConfig.SPEC);

        NeoForge.EVENT_BUS.register(GameEvents.class);
        NeoForge.EVENT_BUS.register(CombatEvents.class);
        NeoForge.EVENT_BUS.register(PlayerEvents.class);
        NeoForge.EVENT_BUS.register(WorldEvents.class);
        NeoForge.EVENT_BUS.register(Champions.class);
        NeoForge.EVENT_BUS.register(ModCommands.class);
        NeoForge.EVENT_BUS.register(PhantomBlocks.class);
        NeoForge.EVENT_BUS.register(BlastTotemState.class);
    }
}
