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

/** Mystery Ore: appraise with a magnifier to reveal a mineral. Better magnifiers find rarer things. */
public class MysteryOreBlock extends Block {
    public MysteryOreBlock(BlockBehaviour.Properties p) { super(p); }
    public static void appraise(ServerLevel level, BlockPos pos, Player player, int magnifier) {
        RandomSource r = level.getRandom();
        boolean deep = level.getBlockState(pos).is(ModBlocks.DEEPSLATE_MYSTERY_ORE.get());
        level.setBlockAndUpdate(pos, deep ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState());
        int luck = Math.min(4, magnifier) + (deep ? 1 : 0);
        ItemStack drop;
        float roll = r.nextFloat() * (1F + luck * 0.25F);
        if (roll < 0.25F) drop = new ItemStack(Items.RAW_IRON, 1 + r.nextInt(3));
        else if (roll < 0.40F) drop = new ItemStack(Items.RAW_GOLD, 1 + r.nextInt(2));
        else if (roll < 0.50F) drop = new ItemStack(ModItems.PLASTIC_GEM.get(), 2 + r.nextInt(3));
        else {
            Trait t = Trait.values()[r.nextInt(Trait.values().length)];
            int lvl = roll > 1.35F ? 3 : roll > 0.95F ? 2 : 1;
            drop = TraitCores.core(t, lvl);
            if (lvl == 3 && r.nextFloat() < 0.2F) drop = new ItemStack(Items.DIAMOND);
        }
        Util2.drop(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
        level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.1);
        if (player instanceof ServerPlayer sp) sp.sendOverlayMessage(Component.translatable("msg.madmodmash.appraised", drop.getHoverName()).withStyle(ChatFormatting.AQUA));
    }
    @Override public void animateTick(BlockState s, Level level, BlockPos pos, RandomSource r) {
        if (r.nextInt(4) == 0) level.addParticle(ParticleTypes.WAX_OFF, pos.getX() + r.nextDouble(), pos.getY() + r.nextDouble(), pos.getZ() + r.nextDouble(), 0, 0, 0);
    }
}
