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

/** Dark Adventures legendary sword: grows with every kill. */
public class LegendarySwordItem extends Item {
    public LegendarySwordItem(Properties p) { super(p); }
    public static int kills(ItemStack s) { CustomData cd = s.get(DataComponents.CUSTOM_DATA); return cd == null ? 0 : cd.copyTag().getIntOr("mm_kills", 0); }
    public static void addKill(ItemStack s, LivingEntity victim) {
        int k = kills(s) + (Champions.isChampion(victim) ? 5 : 1);
        CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("mm_kills", k));
    }
    @Override public float getAttackDamageBonus(Entity victim, float damage, net.minecraft.world.damagesource.DamageSource source) { return Math.min(10F, kills(source.getWeaponItem() == null ? ItemStack.EMPTY : source.getWeaponItem()) / 25F); }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        out.accept(Component.translatable("tooltip.madmodmash.kills", kills(s), String.format("%.1f", Math.min(10F, kills(s) / 25F))).withStyle(ChatFormatting.GOLD));
    }
}
