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

/** Blocks temporarily removed by the Phantom Shovel. */
public final class PhantomBlocks {
    record Ghost(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, long until) {}
    private static final List<Ghost> GHOSTS = new ArrayList<>();
    public static void vanish(ServerLevel level, BlockPos pos, int ticks) {
        GHOSTS.add(new Ghost(level, pos.immutable(), level.getBlockState(pos), level.getGameTime() + ticks));
        level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.01);
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
        if (GHOSTS.isEmpty()) return;
        Iterator<Ghost> it = GHOSTS.iterator();
        while (it.hasNext()) {
            Ghost g = it.next();
            if (g.level.getGameTime() >= g.until) {
                if (g.level.getBlockState(g.pos).canBeReplaced() && g.level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(g.pos)).isEmpty())
                    g.level.setBlockAndUpdate(g.pos, g.state);
                else if (g.level.getGameTime() < g.until + 600) continue;
                it.remove();
            }
        }
    }
    private PhantomBlocks() {}
}
