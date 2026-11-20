package com.madmodmash.system;

import com.madmodmash.entity.Proxy;
import com.madmodmash.entity.VillagerMourner;
import com.madmodmash.item.CarvingKnifeItem;
import com.madmodmash.registry.ModAttachments;
import com.madmodmash.registry.ModEntities;
import com.madmodmash.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;

/**
 * The Unobtainables questlines ("__UnobtainablesQuestlines.txt"). Four stages each:
 * Uncommon, Rare, Epic and finally an Unobtainable reward (the Master Crate & Key).
 */
public final class Quests {
    public enum Line { TRAITOR, BEASTMASTER, BOUNTY_HUNTER, WARTIME, DARKEST_DEPTHS }

    public static int stage(ServerPlayer p, Line line) {
        return ModAttachments.get(p).counter("q_" + line.name());
    }

    /** Complete stage {@code stage} (1..4) if it is the next one. */
    public static void complete(ServerPlayer p, Line line, int stage) {
        PlayerData d = ModAttachments.get(p);
        if (d.counter("q_" + line.name()) != stage - 1) return;
        d.counters.put("q_" + line.name(), stage);
        String key = "quest.madmodmash." + line.name().toLowerCase(java.util.Locale.ROOT) + "." + stage;
        p.sendSystemMessage(Component.translatable("msg.madmodmash.quest_complete", Component.translatable("quest.madmodmash." + line.name().toLowerCase(java.util.Locale.ROOT)),
                Component.translatable(key)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (stage < 4) p.sendSystemMessage(Component.translatable("msg.madmodmash.quest_next", Component.translatable(
                "quest.madmodmash." + line.name().toLowerCase(java.util.Locale.ROOT) + "." + (stage + 1))).withStyle(ChatFormatting.GRAY));
        p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1F, 0.8F + stage * 0.1F);
        switch (stage) {
            case 1 -> { Util2.give(p, new ItemStack(ModItems.IRON_KEY.get())); Economy.give(p, 81 * 3); }
            case 2 -> { Util2.give(p, new ItemStack(ModItems.RARE_HUNT_CONTRACT.get())); Util2.give(p, new ItemStack(ModItems.EMERALD_KEY.get()));
                Engrams.spawn((ServerLevel) p.level(), p.position(), 10); }
            case 3 -> { Util2.give(p, new ItemStack(ModItems.DIAMOND_KEY.get())); Util2.give(p, new ItemStack(randomHalo(p)));
                Engrams.spawn((ServerLevel) p.level(), p.position(), 14); }
            case 4 -> { Util2.give(p, new ItemStack(ModItems.MASTER_CRATE.get())); Util2.give(p, new ItemStack(ModItems.MASTER_KEY.get())); }
        }
        Renown.add(p, 50 * (1 << (stage - 1)), "renown.madmodmash.quest");
    }

    private static net.minecraft.world.item.Item randomHalo(ServerPlayer p) {
        var halos = ModItems.TAB_CONTENTS.get("arcana").stream().map(h -> h.get()).filter(i -> i instanceof com.madmodmash.item.HaloItem).toList();
        return halos.get(p.getRandom().nextInt(halos.size()));
    }

    /** Polled every two seconds from the player tick. */
    public static void poll(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        // Darkest Depths
        if (p.getY() < 0) complete(p, Line.DARKEST_DEPTHS, 1);
        if (level.getBiome(p.blockPosition()).is(Biomes.DEEP_DARK)) complete(p, Line.DARKEST_DEPTHS, 2);
        if (stage(p, Line.DARKEST_DEPTHS) == 2 && !level.getEntities(net.minecraft.world.entity.EntityTypes.WARDEN, p.getBoundingBox().inflate(32), e -> true).isEmpty())
            complete(p, Line.DARKEST_DEPTHS, 3);
        // Traitor: begins in a village at night
        if (stage(p, Line.TRAITOR) == 0 && !level.isBrightOutside() && p.getRandom().nextInt(60) == 0) {
            var poi = level.getPoiManager().findClosest(h -> h.is(PoiTypes.MEETING), p.blockPosition(), 48,
                    net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy.ANY);
            if (poi.isPresent() && !p.getPersistentData().getBooleanOr("mm_traitor_started", false)) startTraitor(p, level, poi.get());
        }
    }

    private static void startTraitor(ServerPlayer p, ServerLevel level, BlockPos bell) {
        p.getPersistentData().putBoolean("mm_traitor_started", true);
        VillagerMourner mourner = Util2.spawn(level, ModEntities.VILLAGER_MOURNER.get(), Util2.surface(level, bell, 2, 6));
        Proxy spy = Util2.spawn(level, ModEntities.PROXY.get(), Util2.surface(level, bell, 6, 20));
        if (spy != null) spy.setPersistenceRequired();
        if (mourner != null) mourner.setPersistenceRequired();
        p.sendSystemMessage(Component.translatable("msg.madmodmash.traitor_start").withStyle(ChatFormatting.DARK_PURPLE));
    }

    // -------------------------------------------------------------- hooks from other systems
    public static void onScan(ServerPlayer p, LivingEntity target) {
        if (target instanceof VillagerMourner) complete(p, Line.TRAITOR, 1);
        if (Champions.isChampion(target)) complete(p, Line.BOUNTY_HUNTER, 2);
        if (target instanceof Villager && stage(p, Line.TRAITOR) == 1) {
            if (ModAttachments.get(p).add("traitor_questioned", 1) >= 3) complete(p, Line.TRAITOR, 2);
        }
        if (target instanceof Proxy proxy && stage(p, Line.TRAITOR) >= 2) {
            complete(p, Line.TRAITOR, 3);
            proxy.reveal(p);
        }
    }

    public static void onKill(ServerPlayer p, LivingEntity victim) {
        if (victim instanceof Proxy && ((Proxy) victim).isRevealed()) complete(p, Line.TRAITOR, 4);
        if (victim.getType() == net.minecraft.world.entity.EntityTypes.WARDEN && p.getMainHandItem().getItem() instanceof CarvingKnifeItem) {
            complete(p, Line.DARKEST_DEPTHS, 3);
            complete(p, Line.DARKEST_DEPTHS, 4);
        }
    }

    public static void onChampionKilled(ServerPlayer p, LivingEntity champion) {
        if (champion.getPersistentData().getBooleanOr(Champions.WANTED, false)) complete(p, Line.BOUNTY_HUNTER, 3);
    }

    public static void onCapture(ServerPlayer p) {
        complete(p, Line.BOUNTY_HUNTER, 4);
    }

    public static void onMeetContractor(ServerPlayer p) {
        complete(p, Line.BOUNTY_HUNTER, 1);
    }

    public static void onTame(ServerPlayer p, LivingEntity animal) {
        PlayerData d = ModAttachments.get(p);
        String key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString();
        d.tamed.add(key);
        complete(p, Line.BEASTMASTER, 1);
        if (d.tamed.size() >= 3) complete(p, Line.BEASTMASTER, 2);
        if (animal.getType() == ModEntities.GOLDEN_EAGLE.get() && d.tamed.size() >= 4) {
            complete(p, Line.BEASTMASTER, 3);
            complete(p, Line.BEASTMASTER, 4);
        }
    }

    public static void onCaptureInBall(ServerPlayer p) {
        if (stage(p, Line.BEASTMASTER) >= 2) complete(p, Line.BEASTMASTER, 3);
    }

    public static void onEventWon(ServerPlayer p, WorldEvents.Type type) {
        if (type.faction == Faction.ILLAGER_HOST) {
            int n = ModAttachments.get(p).add("wartime_wins", 1);
            for (int s = 1; s <= Math.min(4, n); s++) complete(p, Line.WARTIME, s);
        }
    }

    private Quests() {}
    static { ResourceKey.class.getName(); }
}
