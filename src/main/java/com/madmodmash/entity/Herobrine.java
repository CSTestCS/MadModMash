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
 * Herobrine (Duckobrine + the Herobrine Powers List): blinks behind you, vanishes, hurls fire and lightning,
 * tunnels through stone, freezes you in place, raises the dead, and whispers. He haunts the nights while the
 * herobrine_haunting gamerule is on - and only switching it off ends it.
 */
public class Herobrine extends Monster {
    static final String[] WHISPERS = {"msg.madmodmash.herobrine.0", "msg.madmodmash.herobrine.1", "msg.madmodmash.herobrine.2", "msg.madmodmash.herobrine.3", "msg.madmodmash.herobrine.4"};
    int power = 80;
    public Herobrine(EntityType<? extends Monster> type, Level level) { super(type, level); xpReward = 50; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 150).add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ATTACK_DAMAGE, 9)
                .add(Attributes.ARMOR, 10).add(Attributes.FOLLOW_RANGE, 64).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }
    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl)) return;
        if (sl.isBrightOutside() && tickCount > 200 && getTarget() == null) { sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1, getZ(), 30, 0.3, 1, 0.3, 0.02); discard(); return; }
        LivingEntity t = getTarget();
        if (t == null || --power > 0) return;
        power = 60 + getRandom().nextInt(60);
        switch (getRandom().nextInt(8)) {
            case 0 -> { Vec3 behind = t.position().subtract(t.getLookAngle().multiply(2, 0, 2)); teleportTo(behind.x, t.getY(), behind.z); playSound(SoundEvents.ENDERMAN_TELEPORT, 1F, 0.5F); }
            case 1 -> addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
            case 2 -> { var fb = new net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball(sl, this, t.position().subtract(position()).normalize(), 1); fb.setPos(getX(), getEyeY(), getZ()); sl.addFreshEntity(fb); }
            case 3 -> { if (sl.canSeeSky(t.blockPosition())) LightningBoltHelper.strikeReal(sl, t.blockPosition()); }
            case 4 -> t.addEffect(new MobEffectInstance(ModEffects.FROZEN, 60), this);
            case 5 -> { for (int i = 0; i < 2; i++) Util2.spawn(sl, net.minecraft.world.entity.EntityTypes.ZOMBIE, blockPosition().offset(getRandom().nextInt(5) - 2, 0, getRandom().nextInt(5) - 2)); }
            case 6 -> { Vec3 dir = t.position().subtract(position()).normalize(); BlockPos base = blockPosition();
                for (int d = 1; d <= 6; d++) for (int y = 0; y < 2; y++) for (int s = 0; s < 2; s++) {
                    BlockPos p = BlockPos.containing(base.getX() + dir.x * d + s, base.getY() + y, base.getZ() + dir.z * d);
                    var st = sl.getBlockState(p); if (st.getDestroySpeed(sl, p) >= 0 && st.getDestroySpeed(sl, p) < 50 && sl.getBlockEntity(p) == null) sl.destroyBlock(p, false); } }
            default -> { if (t instanceof Player p) p.sendSystemMessage(Component.translatable(WHISPERS[getRandom().nextInt(WHISPERS.length)]).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)); }
        }
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) {
        if (src.getEntity() instanceof Player p && SetBonuses.full(p, "herobrine")) amount *= 0.5F;
        if (getRandom().nextInt(5) == 0 && src.getEntity() != null) { teleportTo(getX() + getRandom().nextInt(9) - 4, getY(), getZ() + getRandom().nextInt(9) - 4); return false; }
        return super.hurtServer(level, src, amount);
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource s, boolean byPlayer) {
        super.dropCustomDeathLoot(level, s, byPlayer);
        spawnAtLocation(level, new ItemStack(ModItems.HEROBRINE_CHARM.get(), 1 + getRandom().nextInt(2)));
        Engrams.spawn(level, position(), 18);
        for (var p : level.players()) p.sendSystemMessage(Component.translatable("msg.madmodmash.herobrine_returns").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
    @Override public boolean removeWhenFarAway(double d) { return false; }
}
