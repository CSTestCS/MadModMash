package com.madmodmash.block;

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

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.context.BlockPlaceContext;

/** Zombie Plague hellrift patch: spreads netherrack and spawns Mortis pigmen. */
public class HellriftBlock extends Block {
    public HellriftBlock(BlockBehaviour.Properties p) { super(p); }
    @Override protected void randomTick(BlockState s, ServerLevel level, BlockPos pos, RandomSource r) {
        BlockPos n = pos.offset(r.nextInt(3) - 1, r.nextInt(3) - 1, r.nextInt(3) - 1);
        if (level.getBlockState(n).is(net.minecraft.tags.BlockTags.DIRT)) level.setBlockAndUpdate(n, Blocks.NETHERRACK.defaultBlockState());
        if (r.nextInt(40) == 0 && level.getEntitiesOfClass(MortisZombie.class, new net.minecraft.world.phys.AABB(pos).inflate(16)).size() < 3)
            Util2.spawn(level, ModEntities.MORTIS_ZOMBIE_PIGMAN.get(), pos.above());
    }
    @Override public void stepOn(Level level, BlockPos pos, BlockState state, Entity e) {
        if (!level.isClientSide() && e instanceof LivingEntity le && !le.fireImmune() && le.tickCount % 20 == 0) le.igniteForSeconds(2);
    }
}
