package com.madmodmash.entity;

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

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
/** AdventureAwaits grapple hook: reels its thrower in, or (sneak-fired) plants a hooked grapple and rope line. */
public class GrappleHook extends ThrowableItemProjectile {
    boolean anchored, ropeMode;
    int anchoredTicks;
    public GrappleHook(EntityType<? extends GrappleHook> type, Level level) { super(type, level); }
    public GrappleHook(EntityType<? extends GrappleHook> type, LivingEntity owner, Level level, ItemStack stack) { super(type, owner, level, stack); }
    @Override protected Item getDefaultItem() { return ModItems.GRAPPLE_HOOK.get(); }
    public static void fire(ServerLevel level, Player p, boolean rope) {
        GrappleHook h = new GrappleHook(ModEntities.GRAPPLE_HOOK.get(), p, level, new ItemStack(ModItems.GRAPPLE_HOOK.get()));
        h.ropeMode = rope;
        h.shootFromRotation(p, p.getXRot(), p.getYRot(), 0F, 2.6F, 0.5F);
        level.addFreshEntity(h);
    }
    @Override protected double getDefaultGravity() { return anchored ? 0 : 0.03; }
    @Override public void tick() {
        if (anchored) {
            setDeltaMovement(Vec3.ZERO);
            if (!(level() instanceof ServerLevel)) return;
            Entity o = getOwner();
            if (!(o instanceof Player p) || ++anchoredTicks > 80 || p.isShiftKeyDown() || p.distanceTo(this) < 1.8 || p.distanceTo(this) > 48) { discard(); return; }
            Vec3 pull = position().subtract(p.position()).normalize().scale(0.55);
            p.setDeltaMovement(pull.x, pull.y + 0.08, pull.z);
            p.syncVelocity = true;
            p.fallDistance = 0;
            return;
        }
        super.tick();
        if (tickCount > 60 && !level().isClientSide()) discard();
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(level() instanceof ServerLevel sl)) return;
        if (ropeMode) {
            BlockPos at = hit.getBlockPos().relative(hit.getDirection());
            if (sl.getBlockState(at).canBeReplaced()) {
                sl.setBlockAndUpdate(at, ModBlocks.HOOKED_GRAPPLE.get().defaultBlockState());
                com.madmodmash.block.RopeBlock.hang(sl, at.below(), 24);
                sl.playSound(null, at, SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 1.6F);
            }
            discard();
            return;
        }
        anchored = true;
        setPos(hit.getLocation());
        sl.playSound(null, hit.getBlockPos(), SoundEvents.CHAIN_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 1F, 1F);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (level() instanceof ServerLevel && getOwner() instanceof Player p && hit.getEntity() instanceof LivingEntity t) {
            Vec3 pull = p.position().subtract(t.position()).normalize().scale(1.2);
            t.push(pull.x, 0.4, pull.z);
            t.syncVelocity = true;
            discard();
        }
    }
}
