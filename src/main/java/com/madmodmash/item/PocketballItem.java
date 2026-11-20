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

/** Battle-pass Pocketball / HG "Summoner": capture a weakened creature, throw again to release it. */
public class PocketballItem extends Item {
    public PocketballItem(Properties p) { super(p); }
    public static boolean full(ItemStack s) { CustomData cd = s.get(DataComponents.CUSTOM_DATA); return cd != null && cd.copyTag().contains("captured"); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel sl) {
            MashProjectile.shoot(sl, player, ModEntities.POCKETBALL.get(), MashProjectile.Kind.POCKETBALL, player.getItemInHand(hand).copyWithCount(1), 1.3F, 0.5F);
            player.getItemInHand(hand).consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
    @Override public boolean isFoil(ItemStack s) { return full(s); }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        CustomData cd = s.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("captured_name")) out.accept(Component.translatable("tooltip.madmodmash.pocketball", cd.copyTag().getStringOr("captured_name", "?")).withStyle(ChatFormatting.AQUA));
    }
}
