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
 * The Bonfire (RPG + Dark Adventures): the hub of the Empire's frontier.
 * - Throw a powder in to radiate one of the Bonfire Effects across its radius.
 * - Use a Recall Token on it to bind recall tokens to it.
 * - Feed it blaze powder or fibrous flame to raise its tier (bigger radius, longer effects).
 * - It warms and slowly heals those who rest beside it, lights the dark, and draws Traveling Merchants.
 */
public class Bonfire extends PathfinderMob {
    static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(Bonfire.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<Integer> EFFECT = SynchedEntityData.defineId(Bonfire.class, EntityDataSerializers.INT);
    UUID owner;
    int effectTicks;
    int visitorTimer;
    public Bonfire(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); setPersistenceRequired(); }
    public static AttributeSupplier.Builder createAttributes() { return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(TIER, 1); b.define(EFFECT, 0); }
    public int getTier() { return entityData.get(TIER); }
    public BonfireEffect getEffect() { return BonfireEffect.values()[Math.max(0, Math.min(BonfireEffect.values().length - 1, entityData.get(EFFECT)))]; }
    public void setOwner(Player p) { owner = p.getUUID(); }
    double radius() { return 6 + getTier() * 4; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) {
        if (src.getEntity() instanceof Player p && p.isShiftKeyDown()) return super.hurtServer(level, src, amount);
        return false;
    }

    @Override public void tick() {
        super.tick();
        setDeltaMovement(0, Math.min(0, getDeltaMovement().y), 0);
        if (!(level() instanceof ServerLevel sl)) return;
        BlockPos at = blockPosition();
        if (tickCount % 40 == 1 && sl.getBlockState(at).isAir()) sl.setBlockAndUpdate(at, net.minecraft.world.level.block.Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 15));
        BonfireEffect fx = getEffect();
        if (tickCount % 4 == 0) {
            int c = fx.color;
            sl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(c, 1.4F), getX(), getY() + 0.6, getZ(), 3, 0.25, 0.3, 0.25, 0.02);
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.4, getZ(), 1, 0.2, 0.1, 0.2, 0.01);
        }
        if (effectTicks > 0 && --effectTicks == 0) entityData.set(EFFECT, 0);
        if (tickCount % 40 == 0) pulse(sl, fx);
        if (++visitorTimer >= MashConfig.VISITOR_INTERVAL.get()) { visitorTimer = 0; summonVisitor(sl, false); }
    }

    private void pulse(ServerLevel sl, BonfireEffect fx) {
        double r = radius();
        var area = getBoundingBox().inflate(r, 4, r);
        List<Player> players = sl.getEntitiesOfClass(Player.class, area);
        List<Mob> hostiles = sl.getEntitiesOfClass(Mob.class, area, m -> m instanceof Enemy);
        for (Player p : players) if (p.getHealth() < p.getMaxHealth() && p.tickCount % 80 < 40) p.heal(1);
        switch (fx) {
            case STRENGTH -> give(players, MobEffects.STRENGTH, 0);
            case SPEED -> give(players, MobEffects.SPEED, 1);
            case RESISTANCE -> give(players, MobEffects.RESISTANCE, 0);
            case REGENERATION -> give(players, MobEffects.REGENERATION, 0);
            case WATER_BREATHING -> give(players, MobEffects.WATER_BREATHING, 0);
            case LEAPING -> { give(players, MobEffects.JUMP_BOOST, 1); give(players, MobEffects.SLOW_FALLING, 0); }
            case GLOWING -> hostiles.forEach(m -> m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80)));
            case INVISIBILITY -> give(players, MobEffects.INVISIBILITY, 0);
            case HEROISM -> give(players, MobEffects.HERO_OF_THE_VILLAGE, 0);
            case NIGHT_VISION -> give(players, MobEffects.NIGHT_VISION, 0);
            case LUCK -> give(players, MobEffects.LUCK, 1);
            case FLIGHT -> players.forEach(p -> p.addEffect(new MobEffectInstance(ModEffects.BONFIRE_FLIGHT, 80, 0, true, true)));
            case FIRE_RESISTANCE -> give(players, MobEffects.FIRE_RESISTANCE, 0);
            case REPULSION -> hostiles.forEach(m -> { Vec3 d = m.position().subtract(position()).normalize(); m.push(d.x * 1.2, 0.3, d.z * 1.2); m.syncVelocity = true; });
            case COMMAND_ANIMALS -> sl.getEntitiesOfClass(Animal.class, area.inflate(8)).forEach(a -> a.getNavigation().moveTo(getX(), getY(), getZ(), 1.0));
            case PROTECTIVE_LIGHT -> hostiles.forEach(m -> { m.igniteForSeconds(2); m.hurtServer(sl, sl.damageSources().magic(), 1); });
            case STASIS -> hostiles.forEach(m -> m.addEffect(new MobEffectInstance(ModEffects.STASIS, 80)));
            case STORM -> { sl.getServer().setWeatherParameters(0, 6000, true, true); entityData.set(EFFECT, 0); }
            case CLEAR_WEATHER -> { sl.getServer().setWeatherParameters(6000, 0, false, false); entityData.set(EFFECT, 0); }
            case SUMMON_VISITOR -> { summonVisitor(sl, true); entityData.set(EFFECT, 0); }
            case CLEANSING -> players.forEach(p -> p.getActiveEffects().stream().filter(e -> !e.getEffect().value().isBeneficial()).map(e -> e.getEffect()).toList().forEach(p::removeEffect));
            case POSSESSION -> sl.getEntitiesOfClass(LivingEntity.class, area, e -> e instanceof TamableAnimal t && t.isTame() || e instanceof AllyGolem || e instanceof VillagerMinion)
                    .forEach(e -> { e.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 80, 1)); e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0)); });
            case GRAVITY -> sl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).forEach(i -> i.setDeltaMovement(position().subtract(i.position()).normalize().scale(0.4)));
            case SMITING -> hostiles.stream().filter(m -> m.is(net.minecraft.tags.EntityTypeTags.UNDEAD)).forEach(m -> m.hurtServer(sl, sl.damageSources().magic(), 3));
            case GROWTH -> { for (int i = 0; i < 6; i++) { BlockPos p = blockPosition().offset(getRandom().nextInt((int) r * 2) - (int) r, getRandom().nextInt(3) - 1, getRandom().nextInt((int) r * 2) - (int) r);
                var st = sl.getBlockState(p); if (st.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock b && b.isValidBonemealTarget(sl, p, st, net.minecraft.world.level.block.BonemealSource.MOB)) b.performBonemeal(sl, getRandom(), p, st, net.minecraft.world.level.block.BonemealSource.MOB); } }
            case POISON -> hostiles.forEach(m -> m.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0)));
            case ELEMENTALISM -> players.forEach(p -> { var arr = new net.minecraft.core.Holder[]{MobEffects.STRENGTH, MobEffects.SPEED, MobEffects.RESISTANCE, MobEffects.FIRE_RESISTANCE};
                p.addEffect(new MobEffectInstance(arr[getRandom().nextInt(arr.length)], 80, 0)); });
            case DISSOLUTION -> hostiles.forEach(m -> { m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1)); m.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0)); });
            default -> {}
        }
    }
    private static void give(List<Player> ps, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> e, int amp) { for (Player p : ps) p.addEffect(new MobEffectInstance(e, 100, amp, true, true)); }

    void summonVisitor(ServerLevel sl, boolean forced) {
        if (!forced && (sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(32)).isEmpty() || !sl.getEntitiesOfClass(TravelingMerchant.class, getBoundingBox().inflate(48)).isEmpty())) return;
        List<EntityType<TravelingMerchant>> types = List.of(ModEntities.TRAVELING_ALCHEMIST.get(), ModEntities.TRAVELING_ARMORER.get(), ModEntities.TRAVELING_ARMSMAN.get(),
                ModEntities.TRAVELING_ARTIFICER.get(), ModEntities.TRAVELING_BANKER.get(), ModEntities.TRAVELING_LOCKSMITH.get());
        TravelingMerchant m = Util2.spawn(sl, types.get(getRandom().nextInt(types.size())), Util2.surface(sl, blockPosition(), 4, 10));
        if (m != null) { m.setDespawnDelay(24000); m.setWanderTarget(blockPosition()); m.setHomeTo(blockPosition(), 12);
            for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(48))) p.sendSystemMessage(Component.translatable("msg.madmodmash.visitor", m.getDisplayName()).withStyle(ChatFormatting.AQUA)); }
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (s.getItem() instanceof PowderItem powder) {
            entityData.set(EFFECT, powder.effect.ordinal());
            effectTicks = 3600 * getTier();
            s.consume(1, player);
            playSound(SoundEvents.FIRECHARGE_USE, 1F, 1.4F);
            player.sendOverlayMessage(Component.translatable("msg.madmodmash.bonfire_infused", Component.translatable(powder.effect.translationKey())).withStyle(ChatFormatting.GOLD));
            return InteractionResult.SUCCESS;
        }
        if (s.getItem() instanceof RecallTokenItem && player instanceof ServerPlayer sp) {
            ModAttachments.get(sp).recall = net.minecraft.core.GlobalPos.of(level().dimension(), blockPosition());
            player.sendOverlayMessage(Component.translatable("msg.madmodmash.recall_bound").withStyle(ChatFormatting.AQUA));
            playSound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1F, 1F);
            return InteractionResult.SUCCESS;
        }
        if ((s.is(Items.BLAZE_POWDER) || s.is(ModItems.FIBROUS_FLAME.get())) && getTier() < 3) {
            entityData.set(TIER, getTier() + 1); s.consume(1, player);
            playSound(SoundEvents.BLAZE_SHOOT, 1F, 1F);
            return InteractionResult.SUCCESS;
        }
        if (s.isEmpty() && player.isShiftKeyDown() && (owner == null || owner.equals(player.getUUID()))) {
            Util2.give(player, new ItemStack(ModItems.BONFIRE_KIT.get()));
            discard();
            return InteractionResult.SUCCESS;
        }
        player.sendOverlayMessage(Component.translatable("msg.madmodmash.bonfire_info", getTier(), Component.translatable(getEffect().translationKey()), effectTicks / 20));
        return InteractionResult.SUCCESS;
    }
    @Override public void remove(RemovalReason reason) {
        if (level() instanceof ServerLevel sl && sl.getBlockState(blockPosition()).is(net.minecraft.world.level.block.Blocks.LIGHT)) sl.removeBlock(blockPosition(), false);
        super.remove(reason);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.putInt("Tier", getTier()); out.putInt("Effect", entityData.get(EFFECT));
        out.putInt("EffectTicks", effectTicks); if (owner != null) out.putString("Owner", owner.toString()); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); entityData.set(TIER, in.getIntOr("Tier", 1)); entityData.set(EFFECT, in.getIntOr("Effect", 0));
        effectTicks = in.getIntOr("EffectTicks", 0); String o = in.getStringOr("Owner", ""); owner = o.isEmpty() ? null : UUID.fromString(o); }
}
