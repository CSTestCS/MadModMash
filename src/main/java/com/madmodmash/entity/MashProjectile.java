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
/** One projectile class for every thrown/fired thing in the mash: rocks, cards, spells, rounds, grenades, pocketballs... */
public class MashProjectile extends ThrowableItemProjectile {
    public enum Kind { ROCK, POP_ROCK, CARD_FIRE, CARD_ICE, CARD_TOXIN, CARD_VOID, CARD_XP, CARD_GOLEM, LUCKY_SPELL, LUCKY_SHURIKEN, THROWING_STAR,
        GRENADE, POCKETBALL, TURRET_BOLT, RIFLE_ROUND, BEAM, LLAMA_SPIT, FIRE_CHARGE, ICE_CHARGE, LIGHTNING_CHARGE }
    public Kind kind = Kind.ROCK;
    public float damage = 3;

    public MashProjectile(EntityType<? extends MashProjectile> type, Level level) { super(type, level); }
    public MashProjectile(EntityType<? extends MashProjectile> type, LivingEntity owner, Level level, ItemStack stack) { super(type, owner, level, stack); }
    @Override protected Item getDefaultItem() { return Items.SNOWBALL; }

    public static MashProjectile shoot(ServerLevel level, LivingEntity shooter, EntityType<MashProjectile> type, Kind kind, ItemStack display, float velocity, float inaccuracy) {
        MashProjectile p = new MashProjectile(type, shooter, level, display);
        p.kind = kind;
        p.damage = switch (kind) { case THROWING_STAR -> 5; case LUCKY_SHURIKEN -> 4; case CARD_FIRE, CARD_VOID -> 3; default -> 3; };
        p.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0F, velocity, inaccuracy);
        level.addFreshEntity(p);
        return p;
    }
    public static MashProjectile shootAt(ServerLevel level, LivingEntity shooter, EntityType<MashProjectile> type, Kind kind, ItemStack display, LivingEntity target, float velocity) {
        MashProjectile p = new MashProjectile(type, shooter, level, display);
        p.kind = kind;
        double dx = target.getX() - shooter.getX(), dy = target.getY(0.4) - p.getY(), dz = target.getZ() - shooter.getZ();
        p.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.1, dz, velocity, 2F);
        level.addFreshEntity(p);
        return p;
    }
    public static void throwPotion(ServerLevel level, LivingEntity shooter, ItemStack potion, LivingEntity target) {
        var tp = new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(level, shooter, potion);
        double dx = target.getX() - shooter.getX(), dy = target.getEyeY() - 1.1 - shooter.getY(), dz = target.getZ() - shooter.getZ();
        tp.setXRot(tp.getXRot() + 20F);
        tp.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 0.75F, 8F);
        level.addFreshEntity(tp);
    }

    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(level() instanceof ServerLevel sl) || !(hit.getEntity() instanceof LivingEntity t)) return;
        Entity owner = getOwner();
        var src = owner instanceof LivingEntity lo ? sl.damageSources().mobProjectile(this, lo) : sl.damageSources().thrown(this, owner);
        RandomSource r = getRandom();
        switch (kind) {
            case POCKETBALL -> { capture(sl, t); return; }
            case CARD_FIRE, FIRE_CHARGE -> { t.igniteForSeconds(5); t.hurtServer(sl, src, kind == Kind.FIRE_CHARGE ? 6 : damage); }
            case CARD_ICE, ICE_CHARGE -> { t.addEffect(new MobEffectInstance(ModEffects.FROZEN, 60), owner); t.hurtServer(sl, src, kind == Kind.ICE_CHARGE ? 5 : damage); }
            case CARD_TOXIN -> { t.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1), owner); placeSludge(sl, t.blockPosition()); }
            case CARD_VOID -> { t.addEffect(new MobEffectInstance(ModEffects.VOID_FIRE, 120), owner); t.hurtServer(sl, src, damage); }
            case CARD_XP -> { if (owner instanceof Player p) p.giveExperiencePoints(7 + r.nextInt(6)); t.hurtServer(sl, src, 2); }
            case CARD_GOLEM -> spawnGolem(sl, t.blockPosition());
            case LUCKY_SPELL, LUCKY_SHURIKEN -> { luckyEffect(t, owner); if (kind == Kind.LUCKY_SHURIKEN) t.hurtServer(sl, src, damage); }
            case LIGHTNING_CHARGE -> { LightningBoltHelper.strikeReal(sl, t.blockPosition()); t.hurtServer(sl, src, 4); }
            case LLAMA_SPIT -> { t.hurtServer(sl, src, damage); t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), owner); }
            case GRENADE, POP_ROCK -> explode(sl);
            default -> t.hurtServer(sl, src, damage);
        }
    }

    @Override protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(level() instanceof ServerLevel sl)) return;
        BlockPos at = hit.getBlockPos().relative(hit.getDirection());
        switch (kind) {
            case GRENADE, POP_ROCK -> explode(sl);
            case CARD_GOLEM -> spawnGolem(sl, at);
            case CARD_TOXIN -> placeSludge(sl, at);
            case FIRE_CHARGE -> { if (sl.getBlockState(at).isAir()) sl.setBlockAndUpdate(at, net.minecraft.world.level.block.BaseFireBlock.getState(sl, at)); }
            case LIGHTNING_CHARGE -> LightningBoltHelper.strikeReal(sl, at);
            case POCKETBALL -> release(sl, at);
            case ROCK, THROWING_STAR -> Util2.drop(sl, getX(), getY(), getZ(), getItem().copyWithCount(1));
            default -> {}
        }
    }

    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide() && !(kind == Kind.POCKETBALL && hit.getType() == HitResult.Type.ENTITY && isRemoved())) discard();
    }

    void explode(ServerLevel sl) {
        sl.explode(this, getX(), getY(), getZ(), kind == Kind.GRENADE ? 2.2F : 1.2F, Level.ExplosionInteraction.NONE);
    }
    void placeSludge(ServerLevel sl, BlockPos p) {
        if (sl.getBlockState(p).canBeReplaced()) sl.setBlockAndUpdate(p, ModBlocks.TOXIC_SLUDGE.get().defaultBlockState());
    }
    void spawnGolem(ServerLevel sl, BlockPos p) {
        SteelGolem g = Util2.spawn(sl, ModEntities.STEEL_GOLEM.get(), p);
        if (g != null) { g.setLifetime(1200); if (getOwner() instanceof Player pl) g.setOwnerPlayer(pl); }
    }
    static void luckyEffect(LivingEntity t, Entity owner) {
        var effects = new net.minecraft.core.Holder[]{MobEffects.LEVITATION, MobEffects.SLOWNESS, MobEffects.POISON, MobEffects.WEAKNESS, MobEffects.GLOWING,
                MobEffects.BLINDNESS, MobEffects.NAUSEA, MobEffects.WITHER, MobEffects.SPEED, MobEffects.REGENERATION, MobEffects.INVISIBILITY};
        @SuppressWarnings("unchecked") var e = (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>) effects[t.getRandom().nextInt(effects.length)];
        t.addEffect(new MobEffectInstance(e, 100, t.getRandom().nextInt(2)), owner);
        if (t.getRandom().nextInt(10) == 0) t.igniteForSeconds(4);
    }

    // ---------------------------------------------------------------- pocketball
    void capture(ServerLevel sl, LivingEntity t) {
        ItemStack ball = getItem().copyWithCount(1);
        boolean beast = getOwner() instanceof ServerPlayer sp && Calling.byName(ModAttachments.get(sp).calling) == Calling.BEASTMASTER;
        if (PocketballItem.full(ball)) { release(sl, t.blockPosition()); return; }
        boolean ok = t instanceof Mob && !(t instanceof Player) && !t.is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)
                && (beast || t.getHealth() <= t.getMaxHealth() * 0.5F || t instanceof Animal);
        if (!ok) { Util2.drop(sl, getX(), getY(), getZ(), ball); return; }
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, sl.registryAccess());
        t.saveWithoutId(out);
        var tag = out.buildResult();
        String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(t.getType()).toString();
        CustomData.update(DataComponents.CUSTOM_DATA, ball, c -> { c.put("captured", tag); c.putString("captured_type", id); c.putString("captured_name", t.getName().getString()); });
        t.discard();
        sl.sendParticles(ParticleTypes.POOF, t.getX(), t.getY() + 0.5, t.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
        Util2.drop(sl, getX(), getY(), getZ(), ball);
        if (getOwner() instanceof ServerPlayer sp) Quests.onCaptureInBall(sp);
        discard();
    }
    void release(ServerLevel sl, BlockPos at) {
        ItemStack ball = getItem().copyWithCount(1);
        CustomData cd = ball.get(DataComponents.CUSTOM_DATA);
        if (cd == null || !cd.copyTag().contains("captured")) { Util2.drop(sl, getX(), getY(), getZ(), ball); return; }
        var tag = cd.copyTag();
        var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse(tag.getStringOr("captured_type", "minecraft:pig")));
        Entity e = type.create(sl, EntitySpawnReason.SPAWN_ITEM_USE);
        if (e != null) {
            e.load(TagValueInput.create(ProblemReporter.DISCARDING, sl.registryAccess(), tag.getCompoundOrEmpty("captured")));
            e.setUUID(java.util.UUID.randomUUID());
            e.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, e.getYRot(), 0);
            sl.addFreshEntity(e);
        }
        Util2.drop(sl, getX(), getY(), getZ(), new ItemStack(ModItems.POCKETBALL.get()));
    }

    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putString("Kind", kind.name()); out.putFloat("Damage", damage); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in);
        try { kind = Kind.valueOf(in.getStringOr("Kind", "ROCK")); } catch (IllegalArgumentException e) { kind = Kind.ROCK; } damage = in.getFloatOr("Damage", 3F); }
}
