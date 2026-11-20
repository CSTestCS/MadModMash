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

/** Dark Adventures claymore slash: a crescent of force that cuts everything it passes. */
public class SlashProjectile extends net.minecraft.world.entity.projectile.Projectile {
    float damage = 8;
    final Set<UUID> hit = new HashSet<>();
    public SlashProjectile(EntityType<? extends SlashProjectile> type, Level level) { super(type, level); setNoGravity(true); }
    public static void slash(ServerLevel level, Player p, float damage) {
        SlashProjectile s = new SlashProjectile(ModEntities.SLASH.get(), level);
        s.setOwner(p);
        s.damage = damage;
        s.setPos(p.getX(), p.getEyeY() - 0.4, p.getZ());
        s.shootFromRotation(p, 0, p.getYRot(), 0F, 1.2F, 0F);
        s.setYRot(p.getYRot());
        level.addFreshEntity(s);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {}
    @Override public void tick() {
        super.tick();
        Vec3 v = getDeltaMovement();
        setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
        if (!(level() instanceof ServerLevel sl)) return;
        if (tickCount > 16) { discard(); return; }
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.3), e -> e != getOwner() && !hit.contains(e.getUUID()))) {
            hit.add(e.getUUID());
            e.hurtServer(sl, sl.damageSources().indirectMagic(this, getOwner()), damage);
        }
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY(), getZ(), 1, 0.2, 0.1, 0.2, 0);
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) { return false; }
}
