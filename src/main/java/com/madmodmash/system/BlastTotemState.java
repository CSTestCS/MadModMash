package com.madmodmash.system;

import com.madmodmash.*;
import com.madmodmash.entity.*;
import com.madmodmash.item.*;
import com.madmodmash.block.*;
import com.madmodmash.registry.*;
import com.madmodmash.system.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BlastTotemState {
    record Armed(ServerLevel level, UUID owner, BlockPos pos, long at) {}
    private static final List<Armed> ARMED = new ArrayList<>();
    public static void arm(ServerLevel level, Player owner, BlockPos pos, int delay) { ARMED.add(new Armed(level, owner.getUUID(), pos, level.getGameTime() + delay)); }
    @net.neoforged.bus.api.SubscribeEvent
    public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
        Iterator<Armed> it = ARMED.iterator();
        while (it.hasNext()) {
            Armed a = it.next();
            if (a.level.getGameTime() % 5 == 0) a.level.sendParticles(ParticleTypes.SMOKE, a.pos.getX() + 0.5, a.pos.getY() + 0.5, a.pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.01);
            if (a.level.getGameTime() < a.at) continue;
            for (LivingEntity le : a.level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(a.pos).inflate(6), le -> !le.getUUID().equals(a.owner))) {
                le.hurtServer(a.level, a.level.damageSources().explosion(null, null), 10F);
                le.push(0, 0.6, 0);
            }
            a.level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, a.pos.getX() + 0.5, a.pos.getY() + 0.5, a.pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            a.level.playSound(null, a.pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1F);
            it.remove();
        }
    }
    private BlastTotemState() {}
}
