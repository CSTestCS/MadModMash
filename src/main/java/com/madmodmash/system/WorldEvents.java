package com.madmodmash.system;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.*;
import com.madmodmash.registry.ModAttachments;
import com.madmodmash.registry.ModBlocks;
import com.madmodmash.registry.ModEntities;
import com.madmodmash.registry.ModGameRules;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Public world events ("Public Events" in the Dark Adventures notes, plus the Wartime questline raids).
 * Wrath decides which faction acts; the event is announced with a boss bar, and ends in victory when its
 * key enemies fall, or in a consequence when time runs out.
 */
public final class WorldEvents extends SavedData {
    public enum Type {
        PILLAGER_CARAVAN(Faction.ILLAGER_HOST, BossEvent.BossBarColor.WHITE, 6000),
        TECHNOMANCER_SIEGE(Faction.ILLAGER_HOST, BossEvent.BossBarColor.RED, 6000),
        MORTIS_OUTBREAK(Faction.MORTIS_BLIGHT, BossEvent.BossBarColor.GREEN, 7200),
        UNHOLY_RITUAL(Faction.UNHOLY, BossEvent.BossBarColor.PURPLE, 3600),
        HEADLESS_NIGHT(Faction.UNHOLY, BossEvent.BossBarColor.YELLOW, 6000),
        NETHER_INVASION(Faction.NETHER, BossEvent.BossBarColor.RED, 6000),
        LOCUST_SWARM(Faction.WILDS, BossEvent.BossBarColor.YELLOW, 4800),
        ROGUE_GOLEM(Faction.WILDS, BossEvent.BossBarColor.RED, 4800),
        WOOFLIN_AMBUSH(Faction.WILDS, BossEvent.BossBarColor.WHITE, 3600),
        METEOR_SHOWER(null, BossEvent.BossBarColor.BLUE, 1600),
        CAVE_IN(null, BossEvent.BossBarColor.WHITE, 400);

        public final Faction faction;
        public final BossEvent.BossBarColor color;
        public final int duration;

        Type(Faction faction, BossEvent.BossBarColor color, int duration) {
            this.faction = faction;
            this.color = color;
            this.duration = duration;
        }

        public String key() { return "event.madmodmash." + name().toLowerCase(Locale.ROOT); }
    }

    static final class Active {
        static final Codec<Active> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("type").forGetter(a -> a.type.name()),
                BlockPos.CODEC.fieldOf("pos").forGetter(a -> a.pos),
                Codec.INT.fieldOf("ticks").forGetter(a -> a.ticks),
                UUIDUtil.STRING_CODEC.listOf().fieldOf("targets").forGetter(a -> new ArrayList<>(a.targets)),
                Codec.INT.fieldOf("total").forGetter(a -> a.total)
        ).apply(i, (t, p, k, tg, tot) -> new Active(Type.valueOf(t), p, k, new HashSet<>(tg), tot)));
        final Type type; final BlockPos pos; int ticks; final Set<UUID> targets; int total;
        transient ServerBossEvent bar;

        Active(Type type, BlockPos pos, int ticks, Set<UUID> targets, int total) {
            this.type = type; this.pos = pos; this.ticks = ticks; this.targets = targets; this.total = total;
        }
    }

    public static final Codec<WorldEvents> CODEC = Active.CODEC.listOf().xmap(WorldEvents::new, w -> w.active);
    public static final SavedDataType<WorldEvents> TYPE = new SavedDataType<>(MadModMash.id("world_events"), WorldEvents::new, CODEC);
    private final List<Active> active;

    public WorldEvents() { this(new ArrayList<>()); }

    private WorldEvents(List<Active> list) { this.active = new ArrayList<>(list); }

    public static WorldEvents get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<String> describe() {
        List<String> out = new ArrayList<>();
        for (Active a : active) out.add(a.type.name() + " @ " + a.pos.toShortString() + " (" + a.ticks / 20 + "s, " + a.targets.size() + "/" + a.total + ")");
        return out;
    }

    // ------------------------------------------------------------------ ticking
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        BorderControl.tick(server);
        if (server.getTickCount() % 20 != 0) return;
        ServerLevel level = server.overworld();
        WorldEvents we = get(server);
        we.tickActive(level);
        if (server.getTickCount() % 1200 == 0) Wrath.get(server).cool();
        if (!level.getGameRules().get(ModGameRules.WORLD_EVENTS.get())) return;
        int interval = MashConfig.EVENT_INTERVAL.get() * 1200;
        for (ServerPlayer p : level.players()) {
            PlayerData d = ModAttachments.get(p);
            int timer = d.add("event_timer", 20);
            if (timer < interval) continue;
            d.counters.put("event_timer", 0);
            we.roll(level, p);
        }
    }

    private void roll(ServerLevel level, ServerPlayer p) {
        if (active.size() >= 3 || p.isSpectator()) return;
        for (Active a : active) if (a.pos.closerThan(p.blockPosition(), 96)) return;
        Wrath wrath = Wrath.get(level);
        Faction f = wrath.angriest();
        double chance = MashConfig.EVENT_CHANCE.get() * (1 + (f == null ? 0 : wrath.tier(f)) * 0.3);
        var r = p.getRandom();
        if (r.nextDouble() > chance) return;
        Type type;
        if (p.getY() < 40 && !level.canSeeSky(p.blockPosition())) type = Type.CAVE_IN;
        else if (!level.isBrightOutside() && r.nextInt(5) == 0) type = Type.METEOR_SHOWER;
        else {
            Faction pick = (f != null && wrath.value(f) > 0 && r.nextFloat() < 0.7F) ? f : Faction.values()[r.nextInt(Faction.values().length)];
            List<Type> opts = Arrays.stream(Type.values()).filter(t -> t.faction == pick).toList();
            if (opts.isEmpty()) return;
            type = opts.get(r.nextInt(opts.size()));
            if (type == Type.HEADLESS_NIGHT && level.isBrightOutside()) type = Type.UNHOLY_RITUAL;
        }
        start(level, type, p.blockPosition());
    }

    public boolean start(ServerLevel level, Type type, BlockPos near) {
        BlockPos c = type == Type.CAVE_IN ? near : Util2.surface(level, near, 24, 40);
        Active a = new Active(type, c, type.duration, new HashSet<>(), 0);
        List<Entity> targets = new ArrayList<>();
        var r = level.getRandom();
        switch (type) {
            case PILLAGER_CARAVAN -> {
                IllagerHero leader = Util2.spawn(level, ModEntities.ILLAGER_HERO.get(), c);
                if (leader != null) { leader.setRole(IllagerHero.Role.CHAMPION); Champions.makeChampion(level, leader, null); targets.add(leader); }
                for (int i = 0; i < 4; i++) { if (r.nextBoolean()) Util2.spawn(level, net.minecraft.world.entity.EntityTypes.PILLAGER, Util2.surface(level, c, 2, 6)); else Util2.spawn(level, net.minecraft.world.entity.EntityTypes.VINDICATOR, Util2.surface(level, c, 2, 6)); }
                for (int i = 0; i < 2; i++) {
                    Villager v = Util2.spawn(level, net.minecraft.world.entity.EntityTypes.VILLAGER, Util2.surface(level, c, 1, 3));
                    if (v != null) { v.getPersistentData().putBoolean("mm_captive", true); v.setNoAi(true); }
                }
            }
            case TECHNOMANCER_SIEGE -> {
                Technomancer t = Util2.spawn(level, ModEntities.TECHNOMANCER.get(), c);
                if (t != null) targets.add(t);
                Nexus n = Util2.spawn(level, ModEntities.REDSTONE_NEXUS.get(), Util2.surface(level, c, 3, 6));
                if (n != null) targets.add(n);
                for (int i = 0; i < 2; i++) Util2.spawn(level, ModEntities.REDSTONE_TURRET.get(), Util2.surface(level, c, 3, 8));
            }
            case MORTIS_OUTBREAK -> {
                for (int i = 0; i < 5; i++) { var z = Util2.spawn(level, ModEntities.MORTIS_ZOMBIE.get(), Util2.surface(level, c, 0, 8)); if (z != null) targets.add(z); }
                Util2.spawn(level, ModEntities.MORTIS_COW.get(), Util2.surface(level, c, 2, 8));
                Util2.spawn(level, ModEntities.MORTIS_CHICKEN.get(), Util2.surface(level, c, 2, 8));
                if (Wrath.get(level).tier(Faction.MORTIS_BLIGHT) >= 3) { var tr = Util2.spawn(level, ModEntities.ZOMBIE_TRUDGER.get(), c); if (tr != null) targets.add(tr); }
            }
            case UNHOLY_RITUAL -> {
                level.setBlockAndUpdate(c, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                level.setBlockAndUpdate(c.above(), Blocks.SOUL_FIRE.defaultBlockState());
                for (int i = 0; i < 4; i++) {
                    double ang = i * Math.PI / 2;
                    BlockPos s = Util2.surface(level, c.offset((int) (Math.cos(ang) * 4), 0, (int) (Math.sin(ang) * 4)), 0, 1);
                    HerobrineCultist cu = Util2.spawn(level, ModEntities.HEROBRINE_CULTIST.get(), s);
                    if (cu != null) { cu.setRitualCenter(c); targets.add(cu); }
                }
            }
            case HEADLESS_NIGHT -> {
                HeadlessHorseman h = Util2.spawn(level, ModEntities.HEADLESS_HORSEMAN.get(), c);
                if (h != null) targets.add(h);
            }
            case NETHER_INVASION -> {
                buildRuinedPortal(level, c);
                for (int i = 0; i < 5; i++) { var pg = Util2.spawn(level, ModEntities.OVERWORLD_PIGLIN.get(), Util2.surface(level, c, 1, 6)); if (pg != null && i < 2) targets.add(pg); }
                var brute = Util2.spawn(level, net.minecraft.world.entity.EntityTypes.PIGLIN_BRUTE, Util2.surface(level, c, 1, 3));
                if (brute != null) { brute.setImmuneToZombification(true); Champions.makeChampion(level, brute, Trait.FIRE); targets.add(brute); }
            }
            case LOCUST_SWARM -> {
                for (int i = 0; i < 3; i++) { var s = Util2.spawn(level, ModEntities.LOCUST_SWARM.get(), Util2.surface(level, c, 0, 10)); if (s != null) targets.add(s); }
                for (int i = 0; i < 6; i++) Util2.spawn(level, ModEntities.LOCUST.get(), Util2.surface(level, c, 0, 10));
            }
            case ROGUE_GOLEM -> {
                RedstoneGolem g = Util2.spawn(level, ModEntities.REDSTONE_GOLEM.get(), c);
                if (g != null) { g.setRogue(true); Champions.makeChampion(level, g, Trait.LIGHTNING); targets.add(g); }
            }
            case WOOFLIN_AMBUSH -> {
                for (int i = 0; i < 4; i++) {
                    var w = Util2.spawn(level, net.minecraft.world.entity.EntityTypes.WOLF, Util2.surface(level, c, 0, 5));
                    if (w != null) { w.startPersistentAngerTimer(); if (i == 0) { Champions.makeAlpha(w); targets.add(w); } }
                }
            }
            case METEOR_SHOWER -> {}
            case CAVE_IN -> {}
        }
        for (Entity e : targets) {
            a.targets.add(e.getUUID());
            if (e instanceof Mob m) m.setPersistenceRequired();
        }
        a.total = a.targets.size();
        active.add(a);
        setDirty();
        Component msg = Component.translatable("msg.madmodmash.event_start", Component.translatable(type.key()), c.getX(), c.getZ())
                .withStyle(type.faction == null ? ChatFormatting.AQUA : type.faction.color);
        for (ServerPlayer pl : Util2.playersNear(level, c, 160)) pl.sendSystemMessage(msg);
        return true;
    }

    private static void buildRuinedPortal(ServerLevel level, BlockPos c) {
        for (int y = 0; y < 5; y++) for (int x = 0; x < 4; x++) {
            boolean frame = x == 0 || x == 3 || y == 0 || y == 4;
            if (frame && level.getRandom().nextInt(5) > 0)
                level.setBlockAndUpdate(c.offset(x + 3, y, 0), level.getRandom().nextBoolean() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
        }
        for (int i = 0; i < 6; i++) level.setBlockAndUpdate(Util2.surface(level, c, 0, 5).below(), Blocks.NETHERRACK.defaultBlockState());
    }

    private void tickActive(ServerLevel level) {
        Iterator<Active> it = active.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            Active a = it.next();
            a.ticks -= 20;
            if (a.bar == null) {
                a.bar = new ServerBossEvent(java.util.UUID.randomUUID(), Component.translatable(a.type.key()), a.type.color, BossEvent.BossBarOverlay.NOTCHED_10);
            }
            List<ServerPlayer> near = Util2.playersNear(level, a.pos, 80);
            for (ServerPlayer p : List.copyOf(a.bar.getPlayers())) if (!near.contains(p)) a.bar.removePlayer(p);
            for (ServerPlayer p : near) a.bar.addPlayer(p);
            a.bar.setProgress(a.total > 0 ? Math.max(0F, a.targets.size() / (float) a.total) : Math.max(0F, a.ticks / (float) a.type.duration));
            tickEffects(level, a);
            boolean won = a.total > 0 && a.targets.isEmpty();
            boolean timeout = a.ticks <= 0;
            if (won || timeout) {
                if (won || a.total == 0) finishWin(level, a, near); else finishFail(level, a, near);
                a.bar.removeAllPlayers();
                it.remove();
                changed = true;
            }
        }
        if (changed) setDirty();
    }

    private void tickEffects(ServerLevel level, Active a) {
        var r = level.getRandom();
        if (a.type == Type.METEOR_SHOWER && r.nextInt(3) == 0) {
            Meteor m = ModEntities.METEOR.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (m != null) {
                BlockPos t = Util2.surface(level, a.pos, 0, 48);
                m.snapTo(t.getX() + r.nextInt(30) - 15, Math.min(level.getMaxY() - 2, t.getY() + 90), t.getZ() + r.nextInt(30) - 15);
                m.setDeltaMovement((t.getX() - m.getX()) / 60.0, -1.6, (t.getZ() - m.getZ()) / 60.0);
                level.addFreshEntity(m);
            }
        }
        if (a.type == Type.CAVE_IN && a.ticks == a.type.duration - 20) {
            for (int i = 0; i < 40; i++) {
                BlockPos p = a.pos.offset(r.nextInt(9) - 4, 2 + r.nextInt(4), r.nextInt(9) - 4);
                if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD))
                    level.setBlockAndUpdate(p, ModBlocks.CRUMBLING_STONE.get().defaultBlockState());
            }
        }
        if (a.type == Type.UNHOLY_RITUAL && r.nextInt(4) == 0)
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, a.pos.getX() + 0.5, a.pos.getY() + 1.5, a.pos.getZ() + 0.5, 6, 2, 1, 2, 0.02);
    }

    private void finishWin(ServerLevel level, Active a, List<ServerPlayer> near) {
        Component msg = Component.translatable("msg.madmodmash.event_won", Component.translatable(a.type.key())).withStyle(ChatFormatting.GREEN);
        for (ServerPlayer p : near) {
            p.sendSystemMessage(msg);
            if (a.total > 0) {
                Renown.add(p, 80, "renown.madmodmash.event");
                Quests.onEventWon(p, a.type);
            }
        }
        if (a.total > 0) {
            int tier = a.type.faction == null ? 6 : 6 + Wrath.get(level).tier(a.type.faction);
            Engrams.spawn(level, net.minecraft.world.phys.Vec3.atCenterOf(a.pos), tier);
            if (a.type.faction != null) Wrath.get(level).add(level.getServer(), a.type.faction, -60);
        }
        if (a.type == Type.PILLAGER_CARAVAN) {
            for (Villager v : Util2.near(level, Villager.class, a.pos, 24, v -> v.getPersistentData().getBooleanOr("mm_captive", false))) {
                v.setNoAi(false);
                v.getPersistentData().remove("mm_captive");
                Economy.drop(level, v.getX(), v.getY() + 1, v.getZ(), 81);
            }
        }
        if (a.type == Type.UNHOLY_RITUAL && level.getBlockState(a.pos.above()).is(Blocks.SOUL_FIRE)) level.removeBlock(a.pos.above(), false);
    }

    private void finishFail(ServerLevel level, Active a, List<ServerPlayer> near) {
        Component msg = Component.translatable("msg.madmodmash.event_lost." + a.type.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_RED);
        for (ServerPlayer p : Util2.playersNear(level, a.pos, 160)) p.sendSystemMessage(msg);
        switch (a.type) {
            case UNHOLY_RITUAL -> {
                int tier = Wrath.get(level).tier(Faction.UNHOLY);
                if (tier >= 6) Util2.spawn(level, net.minecraft.world.entity.EntityTypes.WITHER, a.pos.above(2));
                else {
                    Util2.spawn(level, ModEntities.HEROBRINE.get(), a.pos.above());
                    level.getGameRules().set(ModGameRules.HEROBRINE_HAUNTING.get(), true, level.getServer());
                }
            }
            case MORTIS_OUTBREAK -> {
                for (Villager v : Util2.near(level, Villager.class, a.pos, 64, v -> true))
                    v.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.madmodmash.registry.ModEffects.ZOMBIE_PLAGUE, 1200));
            }
            case PILLAGER_CARAVAN -> {
                for (Villager v : Util2.near(level, Villager.class, a.pos, 24, v -> v.getPersistentData().getBooleanOr("mm_captive", false))) v.discard();
            }
            default -> {}
        }
        if (a.type.faction != null) Wrath.get(level).add(level.getServer(), a.type.faction, 40);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        WorldEvents we = get(level.getServer());
        UUID id = event.getEntity().getUUID();
        for (Active a : we.active) if (a.targets.remove(id)) we.setDirty();
    }

    static { ItemStack.EMPTY.isEmpty(); Items.AIR.getClass(); EquipmentSlot.HEAD.getName(); LivingEntity.class.getName(); Level.class.getName(); }
}
