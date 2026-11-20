package com.madmodmash.system;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/** Assorted helpers shared by systems. */
public final class Util2 {
    public static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
    }

    public static ItemStack stack(String id, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        return new ItemStack(item, count);
    }

    public static void drop(ServerLevel level, double x, double y, double z, ItemStack stack) {
        ItemEntity e = new ItemEntity(level, x, y, z, stack);
        e.setDefaultPickUpDelay();
        level.addFreshEntity(e);
    }

    public static <T extends Entity> List<T> near(ServerLevel level, Class<T> cls, Entity center, double r, Predicate<T> pred) {
        return level.getEntitiesOfClass(cls, new AABB(center.blockPosition()).inflate(r), pred);
    }

    public static <T extends Entity> List<T> near(ServerLevel level, Class<T> cls, BlockPos center, double r, Predicate<T> pred) {
        return level.getEntitiesOfClass(cls, new AABB(center).inflate(r), pred);
    }

    /** Find a surface spawn spot around a centre. */
    public static BlockPos surface(ServerLevel level, BlockPos center, int minR, int maxR) {
        var rand = level.getRandom();
        for (int i = 0; i < 16; i++) {
            double a = rand.nextDouble() * Math.PI * 2;
            int r = minR + rand.nextInt(Math.max(1, maxR - minR + 1));
            int x = center.getX() + (int) (Math.cos(a) * r), z = center.getZ() + (int) (Math.sin(a) * r);
            if (!level.isLoaded(new BlockPos(x, 0, z))) continue;
            BlockPos p = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (level.getBlockState(p.below()).getFluidState().isEmpty()) return p;
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center);
    }

    public static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, BlockPos pos) {
        T mob = type.create(level, EntitySpawnReason.EVENT);
        if (mob == null) return null;
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(mob);
        return mob;
    }

    public static List<ServerPlayer> playersNear(ServerLevel level, BlockPos pos, double r) {
        return level.getPlayers(p -> p.blockPosition().closerThan(pos, r));
    }

    public static void knock(net.minecraft.world.entity.LivingEntity e, double power, double x, double z) {
        e.knockback(power, x, z, e.damageSources().generic(), 0F);
    }

    private Util2() {}
}
