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

/** CSA mystery plant: appraise it with a magnifier to reveal what it really is. */
public class MysteryPlantBlock extends VegetationBlock {
    static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 12, 13);
    public MysteryPlantBlock(BlockBehaviour.Properties p) { super(p); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }
    public static void appraise(ServerLevel level, BlockPos pos, int tier) {
        Block[] common = {ModBlocks.VISTREN_PLANT.get(), ModBlocks.CELERUS_PLANT.get(), ModBlocks.ARMIS_PLANT.get(), ModBlocks.SANATIA_PLANT.get(),
                ModBlocks.VISUS_PLANT.get(), ModBlocks.PATET_PLANT.get(), ModBlocks.ARESCET_PLANT.get(), ModBlocks.SERENUS_PLANT.get(), ModBlocks.VENENUM_PLANT.get()};
        Block[] rare = {ModBlocks.STATIA_PLANT.get(), ModBlocks.TEMPESTA_PLANT.get(), ModBlocks.MORTEM_PLANT.get(), ModBlocks.EMERALD_PLANT.get(), ModBlocks.SHINING_PLANT.get()};
        RandomSource r = level.getRandom();
        Block b = r.nextFloat() < 0.15F + tier * 0.08F ? rare[r.nextInt(rare.length)] : common[r.nextInt(common.length)];
        level.setBlockAndUpdate(pos, b.defaultBlockState());
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.05);
    }
    @Override public void animateTick(BlockState s, Level level, BlockPos pos, RandomSource r) {
        if (r.nextInt(3) == 0) level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.5, pos.getY() + 0.6, pos.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.5, 0, 0.01, 0);
    }
}
