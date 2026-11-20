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

/** Dark Adventures technomancer: an illager who fights with redstone. Summons minions and turrets. */
public class Technomancer extends IllagerHero {
    public Technomancer(EntityType<? extends net.minecraft.world.entity.monster.illager.AbstractIllager> type, Level level) { super(type, level); role = Role.TECHNOMANCER; setOutfit(6); }
    public static AttributeSupplier.Builder createAttributes() { return IllagerHero.createAttributes().add(Attributes.MAX_HEALTH, 50); }
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d, EntitySpawnReason reason, SpawnGroupData data) {
        var r = super.finalizeSpawn(level, d, reason, data); role = Role.TECHNOMANCER; setOutfit(6); equipForRole();
        setCustomName(Component.translatable("entity.madmodmash.technomancer").withStyle(ChatFormatting.RED)); return r;
    }
    @Override protected boolean isRanged() { return true; }
    @Override public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel sl)) return;
        int roll = getRandom().nextInt(3);
        if (roll == 0 && sl.getEntitiesOfClass(RedstoneMinion.class, getBoundingBox().inflate(16)).size() < 4) {
            for (int i = 0; i < 2; i++) Util2.spawn(sl, ModEntities.REDSTONE_MINION.get(), blockPosition().offset(getRandom().nextInt(5) - 2, 0, getRandom().nextInt(5) - 2));
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1, getZ(), 20, 1, 0.5, 1, 0.02);
        } else if (roll == 1 && sl.getEntitiesOfClass(RedstoneTurret.class, getBoundingBox().inflate(16)).size() < 2) {
            Util2.spawn(sl, ModEntities.REDSTONE_TURRET.get(), blockPosition().offset(getRandom().nextInt(7) - 3, 0, getRandom().nextInt(7) - 3));
        } else {
            MashProjectile b = MashProjectile.shootAt(sl, this, ModEntities.TURRET_BOLT.get(), MashProjectile.Kind.TURRET_BOLT, new ItemStack(Items.REDSTONE), target, 1.8F);
            if (b != null) b.damage = 5;
        }
    }
}
