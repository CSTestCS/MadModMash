package com.madmodmash.item;

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

/** Key of Dimensions: travel to the Alphalands and back. */
public class DimensionKeyItem extends Item {
    public static final net.minecraft.resources.ResourceKey<Level> ALPHALANDS = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, MadModMash.id("alphalands"));
    public DimensionKeyItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        var server = sp.level().getServer();
        ServerLevel target;
        BlockPos dest;
        if (level.dimension() == ALPHALANDS) {
            target = server.overworld();
            var respawn = sp.getRespawnConfig();
            dest = respawn != null && respawn.respawnData().dimension() == Level.OVERWORLD ? respawn.respawnData().pos() : target.getRespawnData().pos();
        } else {
            target = server.getLevel(ALPHALANDS);
            if (target == null) return InteractionResult.FAIL;
            dest = new BlockPos(sp.getBlockX(), 0, sp.getBlockZ());
        }
        target.getChunk(dest);
        dest = target.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, dest);
        sp.teleportTo(target, dest.getX() + 0.5, dest.getY() + 0.1, dest.getZ() + 0.5, Set.of(), sp.getYRot(), sp.getXRot(), false);
        target.playSound(null, dest, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.4F, 1.2F);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 200);
        return InteractionResult.SUCCESS;
    }
}
