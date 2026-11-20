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
 * Illager Heroes + the Armies notes: every hero has a Role (Warrior, Archer, Rammer, Bomber, Arsonist,
 * Wizard, Potioneer, Healer, Thief, Assassin, Knight, Champion...), an outfit and one of 35 faces.
 */
public class IllagerHero extends net.minecraft.world.entity.monster.illager.AbstractIllager implements RangedAttackMob {
    public enum Role { WARRIOR, KNIGHT, ROOK, ARCHER, RAMMER, BOMBER, ARSONIST, WIZARD, POTIONEER, HEALER, THIEF, ASSASSIN, CHAMPION, TECHNOMANCER, CULTIST }
    static final EntityDataAccessor<Integer> OUTFIT = SynchedEntityData.defineId(IllagerHero.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<Integer> HEAD = SynchedEntityData.defineId(IllagerHero.class, EntityDataSerializers.INT);
    protected Role role = Role.WARRIOR;
    protected int abilityCooldown = 60;

    public IllagerHero(EntityType<? extends net.minecraft.world.entity.monster.illager.AbstractIllager> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.FOLLOW_RANGE, 32);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(OUTFIT, 0); b.define(HEAD, 0); }
    public int getOutfit() { return entityData.get(OUTFIT); }
    public int getHead() { return entityData.get(HEAD); }
    public Role getRole() { return role; }
    public void setRole(Role r) { role = r; equipForRole(); }

    @Override protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 16F) { @Override public boolean canUse() { return isRanged() && super.canUse(); } });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false) { @Override public boolean canUse() { return !isRanged() && super.canUse(); } });
        this.goalSelector.addGoal(4, new BreakDoorGoal(this, d -> role == Role.RAMMER || role == Role.CHAMPION));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, net.minecraft.world.entity.raid.Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.golem.IronGolem.class, true));
    }
    protected boolean isRanged() { return role == Role.ARCHER || role == Role.WIZARD || role == Role.POTIONEER || role == Role.BOMBER || role == Role.CULTIST; }

    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d, EntitySpawnReason reason, SpawnGroupData data) {
        RandomSource r = getRandom();
        entityData.set(HEAD, r.nextInt(35));
        if (getType() == ModEntities.ILLAGER_HERO.get()) {
            Role[] pool = {Role.WARRIOR, Role.WARRIOR, Role.KNIGHT, Role.ROOK, Role.ARCHER, Role.ARCHER, Role.RAMMER, Role.BOMBER, Role.ARSONIST, Role.WIZARD, Role.POTIONEER, Role.HEALER, Role.THIEF, Role.ASSASSIN};
            role = pool[r.nextInt(pool.length)];
            entityData.set(OUTFIT, switch (role) { case KNIGHT, CHAMPION -> 4; case ROOK, ASSASSIN, THIEF -> 5; case WIZARD, POTIONEER, HEALER -> r.nextBoolean() ? 2 : 3; case WARRIOR, RAMMER -> 1; default -> 0; });
            setCustomName(Component.literal(Champions.NAMES[r.nextInt(Champions.NAMES.length)] + " the " + roleName()).withStyle(ChatFormatting.GRAY));
        }
        equipForRole();
        return super.finalizeSpawn(level, d, reason, data);
    }
    String roleName() { String n = role.name().toLowerCase(Locale.ROOT); return Character.toUpperCase(n.charAt(0)) + n.substring(1); }

    protected void equipForRole() {
        ItemStack main = switch (role) {
            case WARRIOR, CHAMPION -> new ItemStack(Items.IRON_SWORD); case KNIGHT -> new ItemStack(ModItems.STEEL_SWORD.get()); case ROOK, ASSASSIN, THIEF -> new ItemStack(ModItems.WOODEN_DAGGER.get());
            case ARCHER -> new ItemStack(Items.CROSSBOW); case RAMMER -> new ItemStack(ModItems.IRON_HAMMER.get()); case ARSONIST -> new ItemStack(Items.FLINT_AND_STEEL);
            case BOMBER -> new ItemStack(Items.TNT); case WIZARD, CULTIST -> new ItemStack(Items.BLAZE_ROD); case POTIONEER, HEALER -> new ItemStack(Items.SPLASH_POTION);
            case TECHNOMANCER -> new ItemStack(Items.REDSTONE_TORCH);
        };
        setItemSlot(EquipmentSlot.MAINHAND, main);
        setDropChance(EquipmentSlot.MAINHAND, 0.05F);
        if (role == Role.KNIGHT) { setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD)); getAttribute(Attributes.ARMOR).setBaseValue(10); }
        if (role == Role.CHAMPION) { getAttribute(Attributes.MAX_HEALTH).setBaseValue(60); setHealth(60); }
        if (role == Role.ASSASSIN || role == Role.ROOK) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38);
    }

    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl) || --abilityCooldown > 0) return;
        abilityCooldown = 100 + getRandom().nextInt(80);
        LivingEntity t = getTarget();
        switch (role) {
            case HEALER -> { for (var ally : sl.getEntitiesOfClass(net.minecraft.world.entity.raid.Raider.class, getBoundingBox().inflate(10), a -> a.getHealth() < a.getMaxHealth())) {
                ally.heal(6); sl.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY() + 2, ally.getZ(), 3, 0.3, 0.3, 0.3, 0); } }
            case ASSASSIN -> { if (t != null) addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80)); }
            case CHAMPION -> { for (var ally : sl.getEntitiesOfClass(net.minecraft.world.entity.raid.Raider.class, getBoundingBox().inflate(12), a -> a != this))
                ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 0)); }
            case ARSONIST -> { if (t != null && distanceTo(t) < 4) { t.igniteForSeconds(4); BlockPos p = t.blockPosition(); if (sl.getBlockState(p).isAir()) sl.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState()); } }
            default -> {}
        }
    }

    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && role == Role.THIEF && target instanceof Player p) {
            ItemStack stolen = p.getInventory().getSelectedItem();
            int slot = getRandom().nextInt(p.getInventory().getContainerSize());
            stolen = p.getInventory().getItem(slot);
            if (!stolen.isEmpty() && !(stolen.getItem() instanceof net.minecraft.world.item.BlockItem && false)) {
                setItemSlot(EquipmentSlot.OFFHAND, stolen.copy());
                setDropChance(EquipmentSlot.OFFHAND, 2.0F);
                p.getInventory().setItem(slot, ItemStack.EMPTY);
                p.sendOverlayMessage(Component.translatable("msg.madmodmash.thief", stolen.getHoverName()).withStyle(ChatFormatting.RED));
                addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 2));
                setTarget(null);
                goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 16F, 1.2, 1.5));
            }
        }
        return hit;
    }

    @Override public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel sl)) return;
        switch (role) {
            case ARCHER -> { var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(sl, this, new ItemStack(Items.ARROW), getMainHandItem());
                double dx = target.getX() - getX(), dy = target.getY(0.33) - arrow.getY(), dz = target.getZ() - getZ();
                arrow.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.6F, 8F); sl.addFreshEntity(arrow); playSound(SoundEvents.CROSSBOW_SHOOT, 1F, 1F); }
            case WIZARD -> { if (sl.canSeeSky(target.blockPosition()) && getRandom().nextInt(3) == 0) LightningBoltHelper.strikeReal(sl, target.blockPosition());
                else { var fb = new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(sl, this, target.position().subtract(position()).normalize()); fb.setPos(getX(), getEyeY(), getZ()); sl.addFreshEntity(fb); } }
            case POTIONEER -> { var potion = new ItemStack(Items.SPLASH_POTION); potion.set(DataComponents.POTION_CONTENTS, new net.minecraft.world.item.alchemy.PotionContents(
                    getRandom().nextBoolean() ? net.minecraft.world.item.alchemy.Potions.HARMING : net.minecraft.world.item.alchemy.Potions.SLOWNESS));
                MashProjectile.throwPotion(sl, this, potion, target); }
            case BOMBER -> { var tnt = new net.minecraft.world.entity.item.PrimedTnt(sl, getX(), getEyeY(), getZ(), this);
                tnt.setFuse(40); Vec3 v = target.position().subtract(position()); tnt.setDeltaMovement(v.x * 0.08, 0.45, v.z * 0.08); sl.addFreshEntity(tnt); }
            case CULTIST -> { var fb = new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(sl, this, target.position().subtract(position()).normalize()); fb.setPos(getX(), getEyeY(), getZ()); sl.addFreshEntity(fb); }
            default -> {}
        }
    }

    @Override public void applyRaidBuffs(ServerLevel level, int wave, boolean captain) {}
    @Override public net.minecraft.sounds.SoundEvent getCelebrateSound() { return SoundEvents.VINDICATOR_CELEBRATE; }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PILLAGER_HURT; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }
    @Override public IllagerArmPose getArmPose() {
        if (isRanged() && isAggressive()) return role == Role.ARCHER ? IllagerArmPose.CROSSBOW_HOLD : IllagerArmPose.SPELLCASTING;
        return isAggressive() ? IllagerArmPose.ATTACKING : IllagerArmPose.NEUTRAL;
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource s, boolean byPlayer) {
        super.dropCustomDeathLoot(level, s, byPlayer);
        if (byPlayer && getRandom().nextInt(role == Role.CHAMPION ? 1 : 12) == 0) {
            String head = com.madmodmash.registry.ModBlocks.class.getSimpleName();
            var block = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(MadModMash.id("illager_head_" + HEADS[getHead() % HEADS.length]));
            spawnAtLocation(level, new ItemStack(block));
        }
    }
    public static final String[] HEADS = {"base", "armorer", "armorer2", "bandit", "bearded", "brodie", "butcher", "cartographer", "chale", "drunk", "evil",
            "fletcher", "grin", "hansley", "helmet", "herobrine", "illager", "masquerader", "mumbomoustache", "moustache", "odd", "old", "pirate", "scarred",
            "scratchedup", "serene", "shepherd", "skull", "sleepy", "spectacles", "steve", "thief", "villager", "witch", "zal"};
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putString("Role", role.name()); out.putInt("Outfit", getOutfit()); out.putInt("Head", getHead()); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in);
        try { role = Role.valueOf(in.getStringOr("Role", "WARRIOR")); } catch (IllegalArgumentException e) { role = Role.WARRIOR; }
        entityData.set(OUTFIT, in.getIntOr("Outfit", 0)); entityData.set(HEAD, in.getIntOr("Head", 0)); }
    protected void setOutfit(int o) { entityData.set(OUTFIT, o); }
}
