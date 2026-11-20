package com.madmodmash.registry;

import com.google.common.collect.ImmutableSet;
import com.madmodmash.MadModMash;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Emerald Empire's own trades. Trade lists live in data/madmodmash/trade_set and villager_trade. */
public final class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MadModMash.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MadModMash.MODID);

    public static final DeferredHolder<PoiType, PoiType> BANKER_POI = poi("banker", ModBlocks.COIN_BARREL);
    public static final DeferredHolder<PoiType, PoiType> CONTRACTOR_POI = poi("contractor", ModBlocks.CONTRACT_BOARD);
    public static final DeferredHolder<PoiType, PoiType> BOUNTY_BUYER_POI = poi("bounty_buyer", ModBlocks.BOUNTY_BOARD);
    public static final DeferredHolder<PoiType, PoiType> QUARTERMASTER_POI = poi("quartermaster", ModBlocks.IMPERIAL_SUPPLY_CRATE);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> BANKER = prof("banker", BANKER_POI);
    public static final DeferredHolder<VillagerProfession, VillagerProfession> CONTRACTOR = prof("contractor", CONTRACTOR_POI);
    public static final DeferredHolder<VillagerProfession, VillagerProfession> BOUNTY_BUYER = prof("bounty_buyer", BOUNTY_BUYER_POI);
    public static final DeferredHolder<VillagerProfession, VillagerProfession> QUARTERMASTER = prof("quartermaster", QUARTERMASTER_POI);

    private static DeferredHolder<PoiType, PoiType> poi(String name, Supplier<? extends Block> block) {
        return POI_TYPES.register(name, () -> new PoiType(ImmutableSet.copyOf(block.get().getStateDefinition().getPossibleStates()), 1, 1));
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> prof(String name, DeferredHolder<PoiType, PoiType> poi) {
        return PROFESSIONS.register(name, () -> {
            Int2ObjectMap<ResourceKey<TradeSet>> trades = Int2ObjectMap.ofEntries(
                    Int2ObjectMap.entry(1, trade(name, 1)), Int2ObjectMap.entry(2, trade(name, 2)), Int2ObjectMap.entry(3, trade(name, 3)),
                    Int2ObjectMap.entry(4, trade(name, 4)), Int2ObjectMap.entry(5, trade(name, 5)));
            return new VillagerProfession(Component.translatable("entity.madmodmash.villager." + name),
                    h -> h.is(poi.getKey()), h -> h.is(poi.getKey()), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, trades);
        });
    }

    public static ResourceKey<TradeSet> trade(String name, int level) {
        return ResourceKey.create(Registries.TRADE_SET, MadModMash.id(name + "/level_" + level));
    }

    private ModVillagers() {}
}
