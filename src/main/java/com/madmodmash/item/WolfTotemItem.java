package com.madmodmash.item;

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

/** WildLoot / RPG Wolf Totem: slays nearby skeletons and rallies wolves. */
public class WolfTotemItem extends Item {
    public WolfTotemItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel sl) {
            for (var sk : Util2.near(sl, net.minecraft.world.entity.monster.skeleton.AbstractSkeleton.class, player, 16, e -> true)) sk.hurtServer(sl, sl.damageSources().magic(), 1000);
            for (var w : Util2.near(sl, net.minecraft.world.entity.animal.wolf.Wolf.class, player, 24, w -> w.isOwnedBy(player))) {
                w.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1200, 1)); w.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 0)); w.heal(20);
            }
            sl.playSound(null, player.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 1.5F, 0.6F);
            player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 200);
        }
        return InteractionResult.SUCCESS;
    }
}
