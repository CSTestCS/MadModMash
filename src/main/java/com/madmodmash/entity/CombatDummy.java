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

/** Dark Adventures combat dummy: shows every hit. Feed it Mortem powder and it mutates and hits back. */
public class CombatDummy extends Mob {
    public CombatDummy(EntityType<? extends Mob> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() { return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1000).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0); }
    boolean mutant() { return getType() == ModEntities.MUTANT_DUMMY.get(); }
    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) {
        if (src.getEntity() instanceof Player p && p.isShiftKeyDown() && p.getMainHandItem().isEmpty()) {
            Util2.give(p, new ItemStack(ModItems.COMBAT_DUMMY.get())); discard(); return false;
        }
        setCustomName(Component.literal(String.format("%.1f", amount)).withStyle(amount >= 10 ? ChatFormatting.RED : ChatFormatting.YELLOW));
        setCustomNameVisible(true);
        if (mutant() && src.getEntity() instanceof LivingEntity att) att.hurtServer(level, level.damageSources().thorns(this), Math.min(4F, amount * 0.2F));
        boolean r = super.hurtServer(level, src, amount);
        setHealth(getMaxHealth());
        return r;
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (!mutant() && s.is(ModItems.MORTEM_POWDER.get()) && level() instanceof ServerLevel sl) {
            s.consume(1, player);
            Util2.spawn(sl, ModEntities.MUTANT_DUMMY.get(), blockPosition());
            discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
    @Override public boolean isPushable() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
}
