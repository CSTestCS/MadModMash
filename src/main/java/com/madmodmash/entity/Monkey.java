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
import net.minecraft.sounds.SoundEvent;
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
 * MonkeyBusiness monkeys. Wild ones snatch shiny things and run; tame one with bananas and hand it gear:
 * a sword to fight, a pickaxe to forage ore, a crossbow to shoot, a chest to carry, boots to run.
 */
public class Monkey extends TamableAnimal implements RangedAttackMob {
    static final EntityDataAccessor<Integer> GEAR = SynchedEntityData.defineId(Monkey.class, EntityDataSerializers.INT);
    public static final int NONE = 0, SWORD = 1, PICKAXE = 2, CROSSBOW = 3, CHEST = 4, BOOTS = 5;
    private final net.minecraft.world.SimpleContainer pack = new net.minecraft.world.SimpleContainer(9);
    public Monkey(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); setCanPickUpLoot(true); }
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 14).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 3);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(GEAR, NONE); }
    public int getGear() { return entityData.get(GEAR); }
    public void setGear(int g) { entityData.set(GEAR, g);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(g == BOOTS ? 0.42 : 0.3);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(g == SWORD ? 7 : 3); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 30, 12F) { @Override public boolean canUse() { return getGear() == CROSSBOW && super.canUse(); } });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3, true) { @Override public boolean canUse() { return getGear() != CROSSBOW && super.canUse(); } });
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2, 8F, 2F));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.2, s -> s.is(ModItems.BANANA.get()), false));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (!isTame()) {
            if (s.is(ModItems.BANANA.get())) {
                if (!level().isClientSide()) { s.consume(1, player);
                    if (getRandom().nextInt(3) == 0) { tame(player); level().broadcastEntityEvent(this, (byte) 7); setOrderedToSit(false); }
                    else level().broadcastEntityEvent(this, (byte) 6); }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
        if (!isOwnedBy(player)) return InteractionResult.PASS;
        int gear = s.is(net.minecraft.tags.ItemTags.SWORDS) ? SWORD : s.is(net.minecraft.tags.ItemTags.PICKAXES) ? PICKAXE : s.is(Items.CROSSBOW) ? CROSSBOW
                : s.is(Items.CHEST) ? CHEST : s.is(net.minecraft.tags.ItemTags.FOOT_ARMOR) ? BOOTS : -1;
        if (gear >= 0 && gear != getGear()) {
            if (!level().isClientSide()) { setGear(gear); if (!player.isCreative()) s.shrink(1); }
            return InteractionResult.SUCCESS;
        }
        if (getGear() == CHEST && !player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer sp) sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) ->
                    new net.minecraft.world.inventory.ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x1, id, inv, pack, 1), getDisplayName()));
            return InteractionResult.SUCCESS;
        }
        if (s.is(ModItems.BANANA.get()) && getHealth() < getMaxHealth()) { heal(4); s.consume(1, player); return InteractionResult.SUCCESS; }
        if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit());
        return InteractionResult.SUCCESS;
    }
    @Override public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel sl && isTame() && getGear() == PICKAXE && tickCount % 2400 == 0 && getOwner() != null) {
            Item[] finds = {Items.RAW_IRON, Items.COAL, Items.RAW_COPPER, Items.RAW_GOLD, Items.REDSTONE, Items.LAPIS_LAZULI};
            spawnAtLocation(sl, new ItemStack(finds[getRandom().nextInt(finds.length)], 1 + getRandom().nextInt(2)));
        }
    }
    @Override public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel sl)) return;
        var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(sl, this, new ItemStack(Items.ARROW), null);
        double dx = target.getX() - getX(), dy = target.getY(0.33) - arrow.getY(), dz = target.getZ() - getZ();
        arrow.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.6F, 6F);
        sl.addFreshEntity(arrow);
        playSound(SoundEvents.CROSSBOW_SHOOT, 1F, 1F);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putInt("Gear", getGear());
        net.minecraft.world.ContainerHelper.saveAllItems(out, pack.getItems()); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); setGear(in.getIntOr("Gear", NONE));
        net.minecraft.world.ContainerHelper.loadAllItems(in, pack.getItems()); }
    @Override protected void dropEquipment(ServerLevel level) { super.dropEquipment(level); for (ItemStack s : pack.getItems()) if (!s.isEmpty()) spawnAtLocation(level, s); }
    @Override public boolean isFood(ItemStack s) { return s.is(ModItems.BANANA.get()); }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return ModEntities.MONKEY.get().create(level, EntitySpawnReason.BREEDING); }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PARROT_IMITATE_WITCH; }
}
