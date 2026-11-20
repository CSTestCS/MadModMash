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

/** Wutpak vending machine sodas. */
public class SodaItem extends DrinkItem {
    final String kind;
    public SodaItem(Properties p, String kind) { super(p); this.kind = kind; }
    @Override protected void drink(ServerLevel level, Player p) {
        p.getFoodData().eat(3, 0.4F);
        switch (kind) {
            case "creeper_cola" -> { p.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 1)); p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 1200, 0)); }
            case "emerald_fizz" -> { p.addEffect(new MobEffectInstance(MobEffects.LUCK, 2400, 0)); p.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 1200, 0)); }
            default -> { p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1)); p.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0)); }
        }
    }
}
