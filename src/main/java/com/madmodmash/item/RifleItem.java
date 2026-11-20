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

/** Halocraft rifles: hold to fire, reload from ammunition in your inventory. */
public class RifleItem extends Item {
    public enum Kind { ASSAULT(32, 3, 4F, 3.5F), BATTLE(36, 10, 6F, 4.5F), BEAM(6, 25, 16F, 6F);
        final int mag, interval; final float damage, speed;
        Kind(int mag, int interval, float damage, float speed) { this.mag = mag; this.interval = interval; this.damage = damage; this.speed = speed; } }
    public final Kind kind;
    public RifleItem(Properties p, Kind kind) { super(p); this.kind = kind; }
    public static int ammo(ItemStack s) { CustomData cd = s.get(DataComponents.CUSTOM_DATA); return cd == null ? 0 : cd.copyTag().getIntOr("mm_ammo", 0); }
    static void setAmmo(ItemStack s, int a) { CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("mm_ammo", a)); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack s = player.getItemInHand(hand);
        if (ammo(s) <= 0 && !reload(player, s)) {
            if (level instanceof ServerLevel) level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6F, 1.6F);
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    private boolean reload(Player p, ItemStack rifle) {
        Item want = kind == Kind.BEAM ? ModItems.PLASMA_CELL.get() : ModItems.STANDARD_AMMUNITION.get();
        if (!p.isCreative()) {
            int slot = -1;
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (p.getInventory().getItem(i).is(want)) { slot = i; break; }
            if (slot < 0) return false;
            p.getInventory().getItem(slot).shrink(1);
        }
        setAmmo(rifle, kind.mag);
        p.level().playSound(null, p.blockPosition(), ModSounds.RIFLE_EQUIP.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
        p.getCooldowns().addCooldown(rifle, 30);
        return true;
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack s) { return net.minecraft.world.item.ItemUseAnimation.BOW; }
    @Override public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel sl)) return;
        int elapsed = getUseDuration(stack, user) - remaining;
        if (elapsed % kind.interval != 0) return;
        int shots = kind == Kind.BATTLE ? 3 : 1;
        for (int i = 0; i < shots; i++) {
            if (ammo(stack) <= 0) { user.releaseUsingItem(); return; }
            setAmmo(stack, ammo(stack) - 1);
            MashProjectile b = MashProjectile.shoot(sl, user, ModEntities.RIFLE_ROUND.get(), kind == Kind.BEAM ? MashProjectile.Kind.BEAM : MashProjectile.Kind.RIFLE_ROUND,
                    new ItemStack(kind == Kind.BEAM ? ModItems.PLASMA_CELL.get() : ModItems.STANDARD_AMMUNITION.get()), kind.speed, kind == Kind.ASSAULT ? 2.0F : 0.4F);
            if (b != null) { b.damage = kind.damage; b.setNoGravity(true); }
        }
        sl.playSound(null, user.blockPosition(), (kind == Kind.ASSAULT ? ModSounds.ASSAULT_RIFLE_FIRE : kind == Kind.BATTLE ? ModSounds.BATTLE_RIFLE_FIRE : ModSounds.BEAM_RIFLE_FIRE).get(), SoundSource.PLAYERS, 0.6F, 1F);
        if (user instanceof Player p && !p.isCreative()) stack.hurtAndBreak(1, p, p.getUsedItemHand());
    }
    @Override public boolean isBarVisible(ItemStack s) { return true; }
    @Override public int getBarWidth(ItemStack s) { return Math.round(13F * ammo(s) / kind.mag); }
    @Override public int getBarColor(ItemStack s) { return 0xE0C040; }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        out.accept(Component.translatable("tooltip.madmodmash.ammo", ammo(s), kind.mag).withStyle(ChatFormatting.YELLOW));
    }
}
