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

/** Locked crates (RPG / Dark Adventures / Unobtainables Master Crate). */
public class CrateBlock extends FacingBlock {
    public final int tier;
    public CrateBlock(BlockBehaviour.Properties p, int tier) { super(p); this.tier = tier; }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof CrateKeyItem key)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            player.sendOverlayMessage(Component.translatable("msg.madmodmash.crate_locked", tier).withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        int lootTier = tier;
        if (key.tier == -1) {
            if (tier > 3) { player.sendOverlayMessage(Component.translatable("msg.madmodmash.pick_too_weak")); return InteractionResult.FAIL; }
            stack.shrink(1);
            if (player.getRandom().nextFloat() > 0.45F) { sl.playSound(null, pos, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 1F, 1F); return InteractionResult.SUCCESS; }
        } else if (key.tier == -2) {
            if (tier >= 8) return InteractionResult.FAIL;
            lootTier = Math.min(7, tier + 1); stack.shrink(1);
        } else {
            if (key.tier < tier) { player.sendOverlayMessage(Component.translatable("msg.madmodmash.key_too_weak")); return InteractionResult.FAIL; }
            stack.shrink(1);
        }
        open(sl, pos, lootTier, player);
        return InteractionResult.SUCCESS;
    }
    public void open(ServerLevel sl, BlockPos pos, int lootTier, Player player) {
        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this).getPath();
        String table = tier >= 8 ? "crates/master_crate" : name.startsWith("village") || name.startsWith("alpha") ? "crates/" + name : "crates/tier_" + lootTier;
        sl.removeBlock(pos, false);
        LootHelper.drop(sl, table, Vec3.atCenterOf(pos));
        if (lootTier >= 4 && sl.getRandom().nextInt(3) == 0) Engrams.spawn(sl, Vec3.atCenterOf(pos), 6 + lootTier * 2);
        sl.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1F, 0.8F);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 15, 0.4, 0.4, 0.4, 0.05);
        if (player instanceof ServerPlayer sp) Renown.add(sp, 2 + lootTier, "renown.madmodmash.crate");
    }
}
