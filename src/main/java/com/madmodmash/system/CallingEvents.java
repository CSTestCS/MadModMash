package com.madmodmash.system;

import com.madmodmash.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Calling behaviours (the Mc-HG kit list). */
public final class CallingEvents {
    static Calling of(Player p) {
        return Calling.byName(ModAttachments.get(p).calling);
    }

    public static void tick(ServerPlayer p) {
        Calling c = of(p);
        if (c == null) return;
        ServerLevel level = (ServerLevel) p.level();
        switch (c) {
            case POSEIDON -> { if (p.isInWater()) { p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, false)); p.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, true, false));
                if (p.getDeltaMovement().horizontalDistanceSqr() > 0.001) p.setAirSupply(p.getMaxAirSupply()); } }
            case WEREWOLF -> {
                if (!level.isBrightOutside()) { p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, false)); p.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false)); }
                else p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false));
            }
            case FROSTY -> {
                BlockState below = level.getBlockState(p.blockPosition().below());
                if (below.is(BlockTags.ICE) || below.is(Blocks.SNOW_BLOCK) || level.getBlockState(p.blockPosition()).is(Blocks.SNOW))
                    p.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, true, false));
            }
            case FIREMAN -> p.clearFire();
            default -> {}
        }
    }

    /** Every tick: Kangaroo double jump. */
    public static void fastTick(ServerPlayer p) {
        if (of(p) != Calling.KANGAROO) return;
        PlayerData d = ModAttachments.get(p);
        boolean jump = p.getLastClientInput().jump();
        if (p.onGround()) d.doubleJumped = false;
        else if (jump && !d.jumpHeld && !d.doubleJumped && !p.getAbilities().flying && !p.isInWater()) {
            d.doubleJumped = true;
            Vec3 look = p.getLookAngle();
            p.setDeltaMovement(look.x * 0.35, 0.62, look.z * 0.35);
            p.syncVelocity = true;
            p.fallDistance = 0;
            ((ServerLevel) p.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 8, 0.3, 0.1, 0.3, 0.02);
        }
        d.jumpHeld = jump;
    }

    @SubscribeEvent
    public static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        Player p = event.getPlayer();
        if (!(event.getLevel() instanceof ServerLevel level) || p.isShiftKeyDown()) return;
        Calling c = of(p);
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (c == Calling.LUMBERJACK && state.is(BlockTags.LOGS)) {
            java.util.ArrayDeque<BlockPos> q = new java.util.ArrayDeque<>();
            java.util.Set<BlockPos> seen = new java.util.HashSet<>();
            q.add(pos);
            int broken = 0;
            while (!q.isEmpty() && broken < 96) {
                BlockPos b = q.poll();
                for (BlockPos n : BlockPos.betweenClosed(b.offset(-1, 0, -1), b.offset(1, 1, 1))) {
                    BlockPos im = n.immutable();
                    if (seen.add(im) && !im.equals(pos) && level.getBlockState(im).is(BlockTags.LOGS)) {
                        level.destroyBlock(im, true, p);
                        q.add(im);
                        broken++;
                    }
                }
            }
        }
        if (c == Calling.JACKHAMMER && state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            for (int y = 1; y <= 4; y++) {
                BlockPos b = pos.above(y);
                if (level.getBlockState(b).is(BlockTags.BASE_STONE_OVERWORLD)) level.destroyBlock(b, true, p); else break;
            }
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof Player p && of(p) == Calling.FLETCHER && event.getState().is(Blocks.GRAVEL)) {
            event.getDrops().clear();
            event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(event.getLevel(), event.getPos().getX() + 0.5, event.getPos().getY() + 0.5,
                    event.getPos().getZ() + 0.5, new ItemStack(Items.FLINT)));
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (of(event.getEntity()) == Calling.WORM && event.getState().is(BlockTags.DIRT)) event.setNewSpeed(event.getOriginalSpeed() * 50);
        if (of(event.getEntity()) == Calling.MINER && event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE)) event.setNewSpeed(event.getNewSpeed() * 1.15F);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player p) || of(p) != Calling.CULTIVATOR || !(event.getLevel() instanceof ServerLevel level)) return;
        BlockState s = event.getPlacedBlock();
        if (s.getBlock() instanceof BonemealableBlock b && (s.is(BlockTags.SAPLINGS) || s.is(BlockTags.CROPS))) {
            BlockPos pos = event.getPos();
            level.getServer().execute(() -> {
                for (int i = 0; i < 8; i++) {
                    BlockState cur = level.getBlockState(pos);
                    if (cur.getBlock() instanceof BonemealableBlock bb && bb.isValidBonemealTarget(level, pos, cur, net.minecraft.world.level.block.BonemealSource.INTERACTION)) bb.performBonemeal(level, level.getRandom(), pos, cur, net.minecraft.world.level.block.BonemealSource.INTERACTION);
                    else break;
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player p && of(p) == Calling.MINER && event.getItem().is(Items.APPLE) && p.getFoodData().getFoodLevel() >= 20)
            p.addEffect(new MobEffectInstance(MobEffects.HASTE, 1200, 1));
    }

    private CallingEvents() {}
}
