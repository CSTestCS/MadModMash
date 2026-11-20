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
 * The Eighth Plague: locusts eat crops; a well-fed locust becomes a Swarm, a fed swarm becomes a Horde.
 */
public class Locust extends Monster {
    int growth;
    public Locust(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 20, true);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 4).add(Attributes.FLYING_SPEED, 0.6).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 1);
    }
    public static AttributeSupplier.Builder createSwarmAttributes() { return createAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ATTACK_DAMAGE, 3); }
    public static AttributeSupplier.Builder createHordeAttributes() { return createAttributes().add(Attributes.MAX_HEALTH, 60).add(Attributes.ATTACK_DAMAGE, 5); }
    @Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) { return new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this, level); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    boolean isSwarm() { return getType() == ModEntities.LOCUST_SWARM.get(); }
    boolean isHorde() { return getType() == ModEntities.LOCUST_HORDE.get(); }
    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl) || tickCount % 20 != 0) return;
        int r = isHorde() ? 3 : isSwarm() ? 2 : 1;
        BlockPos c = blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-r, -2, -r), c.offset(r, 1, r))) {
            var st = sl.getBlockState(p);
            if (st.getBlock() instanceof net.minecraft.world.level.block.CropBlock || st.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS) || st.is(net.minecraft.tags.BlockTags.LEAVES)) {
                sl.destroyBlock(p, false, this);
                playSound(SoundEvents.GENERIC_EAT.value(), 0.5F, 1.4F);
                if (++growth >= 12) grow(sl);
                break;
            }
        }
    }
    void grow(ServerLevel sl) {
        EntityType<? extends Locust> next = isHorde() ? null : isSwarm() ? ModEntities.LOCUST_HORDE.get() : ModEntities.LOCUST_SWARM.get();
        if (next == null) { growth = 0; return; }
        Locust l = Util2.spawn(sl, next, blockPosition());
        if (l != null) discard();
    }
    @Override public void die(DamageSource src) {
        if (level() instanceof ServerLevel sl && (isSwarm() || isHorde()))
            for (int i = 0; i < (isHorde() ? 3 : 4); i++) Util2.spawn(sl, isHorde() ? ModEntities.LOCUST_SWARM.get() : ModEntities.LOCUST.get(), blockPosition());
        super.die(src);
    }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity le) le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100), this);
        return hit;
    }
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState s, BlockPos p) {}
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return SoundEvents.BEE_LOOP; }
}
