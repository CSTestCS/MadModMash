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

/** Herobrine cultists keep the Unholy Ritual burning. */
public class HerobrineCultist extends IllagerHero {
    BlockPos center;
    public HerobrineCultist(EntityType<? extends net.minecraft.world.entity.monster.illager.AbstractIllager> type, Level level) { super(type, level); role = Role.CULTIST; setOutfit(7); }
    public static AttributeSupplier.Builder createAttributes() { return IllagerHero.createAttributes().add(Attributes.MAX_HEALTH, 26); }
    public void setRitualCenter(BlockPos c) { center = c; setHomeTo(c, 6); }
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance d, EntitySpawnReason reason, SpawnGroupData data) {
        var r = super.finalizeSpawn(level, d, reason, data); role = Role.CULTIST; setOutfit(7); equipForRole();
        setCustomName(Component.translatable("entity.madmodmash.herobrine_cultist").withStyle(ChatFormatting.DARK_PURPLE)); return r;
    }
    @Override protected boolean isRanged() { return true; }
    @Override public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel sl && center != null && tickCount % 10 == 0)
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 2.2, getZ(), 2, 0.1, 0.1, 0.1, 0.01);
    }
}
