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

/** Quackthstone magic cards, thrown like spells. */
public class MagicCardItem extends Item {
    public enum Kind { FIRE, ICE, TOXIN, VOID_FIRE, XP, HEALING, GOLEM }
    public final Kind kind;
    public MagicCardItem(Properties p, Kind kind) { super(p); this.kind = kind; }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            if (kind == Kind.HEALING) { player.heal(8); player.removeEffect(MobEffects.POISON); sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.5, player.getZ(), 6, 0.4, 0.4, 0.4, 0); }
            else {
                MashProjectile.Kind pk = switch (kind) { case FIRE -> MashProjectile.Kind.CARD_FIRE; case ICE -> MashProjectile.Kind.CARD_ICE; case TOXIN -> MashProjectile.Kind.CARD_TOXIN;
                    case VOID_FIRE -> MashProjectile.Kind.CARD_VOID; case XP -> MashProjectile.Kind.CARD_XP; default -> MashProjectile.Kind.CARD_GOLEM; };
                MashProjectile.shoot(sl, player, ModEntities.MAGIC_CARD.get(), pk, stack.copyWithCount(1), 1.6F, 0.5F);
            }
            sl.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1F, 1.4F);
            boolean wizard = SetBonuses.full(player, "wizard");
            if (!player.isCreative() && !(wizard && player.getRandom().nextBoolean())) stack.shrink(1);
            player.getCooldowns().addCooldown(stack, 10);
        }
        return InteractionResult.SUCCESS;
    }
}
