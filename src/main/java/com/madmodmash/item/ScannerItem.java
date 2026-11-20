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

/** ModJelly bounty scanner: reveals Champions, bounties and disguised Spies. */
public class ScannerItem extends Item {
    public ScannerItem(Properties p) { super(p); }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        describe(sp, target);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0), player);
        Quests.onScan(sp, target);
        if (Champions.isChampion(target) && !player.isShiftKeyDown()) {
            ItemStack card = new ItemStack(ModItems.KILL_CARD.get());
            TargetCardItem.bind(card, target);
            Util2.give(player, card);
        } else if (Champions.isChampion(target)) {
            ItemStack card = new ItemStack(ModItems.CAPTURE_CARD.get());
            TargetCardItem.bind(card, target);
            Util2.give(player, card);
        }
        player.getCooldowns().addCooldown(stack, 20);
        player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1F, 1.6F);
        return InteractionResult.SUCCESS;
    }
    public static void describe(ServerPlayer p, LivingEntity target) {
        p.sendSystemMessage(Component.translatable("msg.madmodmash.scan_head", target.getName(), (int) target.getHealth(), (int) target.getMaxHealth()).withStyle(ChatFormatting.AQUA));
        Faction f = Faction.of(target);
        if (f != null) p.sendSystemMessage(Component.translatable("msg.madmodmash.scan_faction", f.title).withStyle(f.color));
        if (Champions.isChampion(target)) p.sendSystemMessage(Component.translatable("msg.madmodmash.scan_champion").withStyle(ChatFormatting.GOLD));
        Trait t = Champions.trait(target);
        if (t != null) p.sendSystemMessage(Component.translatable("msg.madmodmash.scan_trait", Component.translatable("trait.madmodmash." + t.name().toLowerCase(Locale.ROOT))).withStyle(t.color));
        if (target.getPersistentData().getBooleanOr(Champions.WANTED, false)) p.sendSystemMessage(Component.translatable("msg.madmodmash.scan_wanted").withStyle(ChatFormatting.RED));
        if (target instanceof Proxy proxy && !proxy.isRevealed()) p.sendSystemMessage(Component.translatable("msg.madmodmash.scan_disguise").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
