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

/** Air/Climbing Hook: thrusts you high into the air when used against a block. */
public class AirHookItem extends Item {
    public AirHookItem(Properties p) { super(p); }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Player p = ctx.getPlayer();
        if (p == null) return InteractionResult.PASS;
        Vec3 v = p.getDeltaMovement();
        p.setDeltaMovement(v.x * 1.3, 1.25, v.z * 1.3);
        p.syncVelocity = true;
        if (ctx.getLevel() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 12, 0.3, 0.1, 0.3, 0.05);
            ctx.getItemInHand().hurtAndBreak(1, p, ctx.getHand());
            p.getCooldowns().addCooldown(ctx.getItemInHand(), 30);
        }
        return InteractionResult.SUCCESS;
    }
}
