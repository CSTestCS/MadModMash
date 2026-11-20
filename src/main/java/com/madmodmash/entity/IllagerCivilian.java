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
 * NewMobs illager civilians (butcher, smith, farmer, priest, librarian, nitwit). They keep to themselves
 * and will barter for emeralds - unless the Illager Host's Wrath runs high, when they turn on you.
 */
public class IllagerCivilian extends PathfinderMob {
    public IllagerCivilian(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 22).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 20);
    }
    String trade() { return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath().replace("illager_", ""); }
    boolean hostile() { return level() instanceof ServerLevel sl && Wrath.get(sl).tier(Faction.ILLAGER_HOST) >= 3; }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (e, l) -> hostile()));
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (!s.is(Items.EMERALD) || hostile()) return super.mobInteract(player, hand);
        if (level() instanceof ServerLevel) {
            s.shrink(1);
            RandomSource r = getRandom();
            ItemStack give = switch (trade()) {
                case "butcher" -> new ItemStack(r.nextBoolean() ? Items.COOKED_BEEF : ModItems.ALPHA_STEAK.get(), 2 + r.nextInt(3));
                case "smith" -> new ItemStack(r.nextBoolean() ? ModItems.STEEL_INGOT.get() : ModItems.CHAINMAIL_SHEET.get(), 1 + r.nextInt(2));
                case "farmer" -> new ItemStack(r.nextBoolean() ? Items.BREAD : ModItems.BANANA.get(), 3 + r.nextInt(4));
                case "priest" -> new ItemStack(r.nextBoolean() ? ModItems.ANTIPOTION.get() : ModItems.RITUAL_CANDLE.get());
                case "librarian" -> new ItemStack(r.nextBoolean() ? ModItems.LUCKY_SPELL.get() : ModItems.BLANK_CONTRACT.get());
                default -> new ItemStack(r.nextBoolean() ? ModItems.ROCK.get() : Items.STICK, 1 + r.nextInt(6));
            };
            Util2.give(player, give);
            playSound(SoundEvents.PILLAGER_CELEBRATE, 1F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }
}
