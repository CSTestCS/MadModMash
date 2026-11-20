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

/** HmmMod villager statues. */
public class VillagerStatueBlock extends FacingBlock {
    static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 16, 13);
    public VillagerStatueBlock(BlockBehaviour.Properties p) { super(p); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }
    private static Map<Block, Supplier<Block>> MAP;
    public static Block statueFor(Block b, boolean precious) {
        if (MAP == null) {
            MAP = new HashMap<>();
            MAP.put(Blocks.STONE, ModBlocks.STONE_VILLAGER_STATUE); MAP.put(Blocks.COBBLESTONE, ModBlocks.STONE_VILLAGER_STATUE);
            MAP.put(Blocks.SMOOTH_STONE, ModBlocks.SMOOTH_STONE_VILLAGER_STATUE); MAP.put(Blocks.GRANITE, ModBlocks.GRANITE_VILLAGER_STATUE);
            MAP.put(Blocks.POLISHED_GRANITE, ModBlocks.POLISHED_GRANITE_VILLAGER_STATUE); MAP.put(Blocks.DIORITE, ModBlocks.DIORITE_VILLAGER_STATUE);
            MAP.put(Blocks.POLISHED_DIORITE, ModBlocks.POLISHED_DIORITE_VILLAGER_STATUE); MAP.put(Blocks.ANDESITE, ModBlocks.ANDESITE_VILLAGER_STATUE);
            MAP.put(Blocks.POLISHED_ANDESITE, ModBlocks.POLISHED_ANDESITE_VILLAGER_STATUE); MAP.put(Blocks.MOSSY_COBBLESTONE, ModBlocks.MOSSY_VILLAGER_STATUE);
            MAP.put(Blocks.SANDSTONE, ModBlocks.SANDSTONE_VILLAGER_STATUE); MAP.put(Blocks.RED_SANDSTONE, ModBlocks.RED_SANDSTONE_VILLAGER_STATUE);
            MAP.put(Blocks.PRISMARINE, ModBlocks.PRISMARINE_VILLAGER_STATUE); MAP.put(Blocks.DARK_PRISMARINE, ModBlocks.DARK_PRISMARINE_VILLAGER_STATUE);
            MAP.put(Blocks.QUARTZ_BLOCK, ModBlocks.QUARTZ_VILLAGER_STATUE); MAP.put(Blocks.OBSIDIAN, ModBlocks.OBSIDIAN_VILLAGER_STATUE);
            MAP.put(Blocks.SNOW_BLOCK, ModBlocks.SNOW_VILLAGER_STATUE); MAP.put(Blocks.IRON_BLOCK, ModBlocks.IRON_VILLAGER_STATUE);
            MAP.put(Blocks.GOLD_BLOCK, ModBlocks.GOLD_VILLAGER_STATUE); MAP.put(Blocks.LAPIS_BLOCK, ModBlocks.LAPIS_VILLAGER_STATUE);
            MAP.put(Blocks.EMERALD_BLOCK, ModBlocks.EMERALD_VILLAGER_STATUE); MAP.put(Blocks.DIAMOND_BLOCK, ModBlocks.DIAMOND_VILLAGER_STATUE);
            MAP.put(Blocks.BEDROCK, ModBlocks.BEDROCK_VILLAGER_STATUE); MAP.put(Blocks.TUFF, ModBlocks.TUFF_VILLAGER_STATUE);
            MAP.put(Blocks.POLISHED_DEEPSLATE, ModBlocks.DEEPSLATE_VILLAGER_STATUE); MAP.put(Blocks.DEEPSLATE, ModBlocks.DEEPSLATE_VILLAGER_STATUE);
            MAP.put(Blocks.POLISHED_BLACKSTONE, ModBlocks.BLACKSTONE_VILLAGER_STATUE); MAP.put(Blocks.BLACKSTONE, ModBlocks.BLACKSTONE_VILLAGER_STATUE);
            MAP.put(Blocks.CALCITE, ModBlocks.CALCITE_VILLAGER_STATUE); MAP.put(Blocks.END_STONE, ModBlocks.END_STONE_VILLAGER_STATUE);
            MAP.put(Blocks.NETHERRACK, ModBlocks.NETHERRACK_VILLAGER_STATUE); MAP.put(ModBlocks.ASTROITE.get(), ModBlocks.ASTROITE_VILLAGER_STATUE);
            MAP.put(ModBlocks.RUBY_BLOCK.get(), ModBlocks.RUBY_VILLAGER_STATUE);
        }
        boolean isPrecious = b == Blocks.IRON_BLOCK || b == Blocks.GOLD_BLOCK || b == Blocks.EMERALD_BLOCK || b == Blocks.DIAMOND_BLOCK || b == Blocks.LAPIS_BLOCK || b == Blocks.BEDROCK || b == ModBlocks.RUBY_BLOCK.get();
        if (isPrecious != precious) return null;
        Supplier<Block> s = MAP.get(b);
        return s == null ? null : s.get();
    }
}
