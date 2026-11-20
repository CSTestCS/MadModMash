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

/** Flub material slimes: every block-flavoured slime drops what it is made of. */
public class MaterialSlime extends net.minecraft.world.entity.monster.cubemob.Slime {
    public MaterialSlime(EntityType<? extends net.minecraft.world.entity.monster.cubemob.Slime> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() { return Monster.createMonsterAttributes(); }
    String material() { return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath().replace("_slime", ""); }
    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource src, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, src, killedByPlayer);
        if (getSize() > 1) return;
        RandomSource r = getRandom();
        ItemStack drop = switch (material()) {
            case "stone" -> new ItemStack(Items.COBBLESTONE, 1 + r.nextInt(2)); case "sand" -> new ItemStack(Items.SAND, 1 + r.nextInt(2));
            case "wood" -> new ItemStack(Items.OAK_LOG); case "earth" -> new ItemStack(Items.DIRT, 2); case "coal" -> new ItemStack(Items.COAL, 1 + r.nextInt(2));
            case "iron" -> new ItemStack(Items.IRON_NUGGET, 2 + r.nextInt(4)); case "gold" -> new ItemStack(Items.GOLD_NUGGET, 2 + r.nextInt(4));
            case "redstone" -> new ItemStack(Items.REDSTONE, 2 + r.nextInt(3)); case "lapis" -> new ItemStack(Items.LAPIS_LAZULI, 1 + r.nextInt(3));
            case "diamond" -> r.nextInt(5) == 0 ? new ItemStack(Items.DIAMOND) : ItemStack.EMPTY; case "emerald" -> r.nextInt(3) == 0 ? new ItemStack(Items.EMERALD) : ItemStack.EMPTY;
            case "obsidian" -> new ItemStack(Items.OBSIDIAN); case "ice" -> new ItemStack(Items.ICE); case "lava" -> new ItemStack(Items.MAGMA_CREAM);
            case "honey" -> new ItemStack(Items.HONEYCOMB); case "glass" -> new ItemStack(Items.GLASS); case "glowstone" -> new ItemStack(Items.GLOWSTONE_DUST, 2);
            case "netherrack" -> new ItemStack(Items.NETHERRACK, 2); case "soul" -> new ItemStack(Items.SOUL_SAND); case "prismarine" -> new ItemStack(Items.PRISMARINE_SHARD, 2);
            case "netherite" -> r.nextInt(12) == 0 ? new ItemStack(Items.NETHERITE_SCRAP) : new ItemStack(Items.GOLD_NUGGET, 3);
            default -> new ItemStack(Items.SLIME_BALL);
        };
        if (!drop.isEmpty()) spawnAtLocation(level, drop);
    }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity le) switch (material()) {
            case "lava", "netherrack" -> le.igniteForSeconds(3);
            case "soul" -> le.igniteForSeconds(5);
            case "ice" -> le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), this);
            case "honey" -> le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2), this);
            case "glowstone" -> le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100), this);
            case "redstone" -> Util2.knock(le, 0.6, getX() - le.getX(), getZ() - le.getZ());
            default -> {}
        }
        return hit;
    }
}
