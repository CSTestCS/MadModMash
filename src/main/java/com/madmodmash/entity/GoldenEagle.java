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

/** AdventureAwaits golden eagle: a hunting bird of the mountains. Tame it with raw meat. */
public class GoldenEagle extends TamableAnimal {
    public GoldenEagle(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 12, false);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.FLYING_SPEED, 0.6).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.FOLLOW_RANGE, 40);
    }
    @Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        var nav = new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false); nav.setCanFloat(true);
        return nav;
    }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.2, 10F, 3F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, net.minecraft.world.entity.animal.rabbit.Rabbit.class, false, null));
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, net.minecraft.world.entity.animal.chicken.Chicken.class, false, null));
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (!isTame() && isFood(s)) {
            if (!level().isClientSide()) { s.consume(1, player); if (getRandom().nextInt(4) == 0) { tame(player); level().broadcastEntityEvent(this, (byte) 7); } else level().broadcastEntityEvent(this, (byte) 6); }
            return InteractionResult.SUCCESS;
        }
        if (isTame() && isOwnedBy(player) && !isFood(s)) { if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit()); return InteractionResult.SUCCESS; }
        return super.mobInteract(player, hand);
    }
    @Override protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState s, BlockPos p) {}
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
    @Override public boolean isFood(ItemStack s) { return s.is(Items.RABBIT) || s.is(Items.CHICKEN) || s.is(Items.SALMON) || s.is(Items.COD); }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return ModEntities.GOLDEN_EAGLE.get().create(level, EntitySpawnReason.BREEDING); }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return SoundEvents.PARROT_AMBIENT; }
}
