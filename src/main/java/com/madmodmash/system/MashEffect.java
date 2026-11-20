package com.madmodmash.system;

import com.madmodmash.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/** A small data-driven MobEffect: an optional tick behaviour every {@code interval} ticks. */
public class MashEffect extends MobEffect {
    public interface Tick { void tick(ServerLevel level, LivingEntity entity, int amplifier); }

    private final int interval;
    private final Tick tick;

    public MashEffect(MobEffectCategory category, int color, int interval, Tick tick) {
        super(category, color);
        this.interval = interval;
        this.tick = tick;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tick != null && (interval <= 1 || tickCount % interval == 0);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (tick != null) tick.tick(level, entity, amplifier);
        return true;
    }

    // ------------------------------------------------------------------ behaviours
    public static void bleed(ServerLevel level, LivingEntity e, int amp) {
        e.hurtServer(level, level.damageSources().magic(), 1.0F + amp);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
    }

    public static void frozen(ServerLevel level, LivingEntity e, int amp) {
        e.setTicksFrozen(Math.min(e.getTicksRequiredToFreeze() + 20, e.getTicksFrozen() + 3));
        e.setDeltaMovement(0, Math.min(0, e.getDeltaMovement().y), 0);
    }

    public static void voidFire(ServerLevel level, LivingEntity e, int amp) {
        e.hurtServer(level, level.damageSources().magic(), 1.0F + amp);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
        if (e.getRandom().nextInt(4) == 0) {
            double x = e.getX() + (e.getRandom().nextDouble() - 0.5) * 6;
            double z = e.getZ() + (e.getRandom().nextDouble() - 0.5) * 6;
            e.randomTeleport(x, e.getY(), z, true, (BlockState s) -> false);
        }
    }

    public static void plague(ServerLevel level, LivingEntity e, int amp) {
        if (e.getRandom().nextInt(3) == 0) e.hurtServer(level, level.damageSources().magic(), 1.0F);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, e.getX(), e.getY() + 1, e.getZ(), 4, 0.3, 0.4, 0.3, 0.0);
    }

    public static void seerSight(ServerLevel level, LivingEntity e, int amp) {
        if (!(e instanceof ServerPlayer player)) return;
        BlockPos c = e.blockPosition();
        int r = 8 + amp * 4, shown = 0;
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-r, -r, -r), c.offset(r, r, r))) {
            BlockState s = level.getBlockState(p);
            if (s.is(Tags.Blocks.ORES) || s.getBlock() instanceof com.madmodmash.block.MysteryOreBlock) {
                level.sendParticles(player, s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_DIAMOND) ? ParticleTypes.GLOW : ParticleTypes.WAX_ON, true, true,
                        p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.35, 0.35, 0.35, 0.0);
                if (++shown > 60) break;
            }
        }
    }

    public static boolean has(LivingEntity e, net.neoforged.neoforge.registries.DeferredHolder<MobEffect, MobEffect> effect) {
        return e.hasEffect(effect);
    }

    static { ModEffects.class.getName(); }
}
