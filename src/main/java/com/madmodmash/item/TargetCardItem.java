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

/** ModJelly target cards: a bounty on one specific creature. */
public class TargetCardItem extends Item {
    public final boolean capture;
    public TargetCardItem(Properties p, boolean capture) { super(p); this.capture = capture; }
    public static void bind(ItemStack card, LivingEntity target) {
        CustomData.update(DataComponents.CUSTOM_DATA, card, t -> { t.putString("target", target.getUUID().toString()); t.putString("name", target.getName().getString()); });
    }
    public static boolean matches(ItemStack card, LivingEntity e) {
        CustomData cd = card.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.copyTag().getStringOr("target", "").equals(e.getUUID().toString());
    }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        CustomData cd = s.get(DataComponents.CUSTOM_DATA);
        if (cd != null) out.accept(Component.translatable("tooltip.madmodmash.target", cd.copyTag().getStringOr("name", "?")).withStyle(ChatFormatting.GOLD));
        out.accept(Component.translatable(capture ? "tooltip.madmodmash.capture_help" : "tooltip.madmodmash.kill_help").withStyle(ChatFormatting.GRAY));
    }
}
