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

/**
 * CSA companions / Dark Adventures "randomized tamable character": each has a name and a rating in
 * Battle Prowess, Charisma, Crafting, Talent, Legacy, Intelligence and Bravery (0-3), shaping how they fight.
 * Hire one with a Gold Gildan. Hand them a weapon to wield.
 */
public class Companion extends TamableAnimal {
    static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.INT);
    static final String[] STATS = {"prowess", "charisma", "crafting", "talent", "legacy", "intelligence", "bravery"};
    int[] stats = new int[7];
    public Companion(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 24);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(SKIN, 0); }
    public int getSkin() { return entityData.get(SKIN); }
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d, EntitySpawnReason reason, SpawnGroupData data) {
        RandomSource r = getRandom();
        entityData.set(SKIN, r.nextInt(9));
        for (int i = 0; i < 7; i++) stats[i] = r.nextInt(4);
        applyStats();
        setCustomName(Component.literal(Champions.NAMES[r.nextInt(Champions.NAMES.length)]).withStyle(ChatFormatting.GREEN));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(r.nextBoolean() ? Items.STONE_SWORD : ModItems.WOODEN_CLUB.get()));
        return super.finalizeSpawn(level, d, reason, data);
    }
    void applyStats() {
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3 + stats[0] * 1.5);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(20 + stats[6] * 6 + stats[4] * 2);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + stats[3] * 0.02);
    }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.1, 8F, 2F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false, (e, l) -> isTame() && e instanceof Enemy && stats[6] >= 2));
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (!isTame() && s.is(ModItems.GOLD_GILDAN.get())) {
            if (!level().isClientSide()) { s.shrink(1); tame(player); level().broadcastEntityEvent(this, (byte) 7);
                player.sendSystemMessage(Component.translatable("msg.madmodmash.companion_hired", getName())); describe(player); }
            return InteractionResult.SUCCESS;
        }
        if (isTame() && isOwnedBy(player)) {
            if (s.has(DataComponents.WEAPON)) { if (!level().isClientSide()) { spawnAtLocation((ServerLevel) level(), getMainHandItem()); setItemSlot(EquipmentSlot.MAINHAND, s.split(1)); } return InteractionResult.SUCCESS; }
            if (player.isShiftKeyDown()) { if (!level().isClientSide()) describe(player); return InteractionResult.SUCCESS; }
            if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        if (!isTame() && !level().isClientSide()) describe(player);
        return InteractionResult.SUCCESS;
    }
    void describe(Player p) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) sb.append(i > 0 ? ", " : "").append(STATS[i]).append(" ").append(stats[i]);
        p.sendSystemMessage(Component.translatable("msg.madmodmash.companion_stats", getName(), sb.toString()).withStyle(ChatFormatting.AQUA));
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putInt("Skin", getSkin()); out.putIntArray("Stats", stats); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); entityData.set(SKIN, in.getIntOr("Skin", 0));
        in.getIntArray("Stats").ifPresent(a -> { if (a.length == 7) stats = a; }); }
    @Override public boolean isFood(ItemStack s) { return false; }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }
    @Override public boolean canMate(Animal other) { return false; }
}
