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

/** ModJam July 23 Wanted Poster: names a Champion in the wild and points you toward it. */
public class WantedPosterItem extends Item {
    public WantedPosterItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        ItemStack poster = player.getItemInHand(hand);
        CustomData cd = poster.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("target")) {
            var tag = cd.copyTag();
            UUID id = UUID.fromString(tag.getStringOr("target", ""));
            Entity e = sl.getEntity(id);
            if (e != null) {
                CustomData.update(DataComponents.CUSTOM_DATA, poster, t -> { t.putInt("x", e.getBlockX()); t.putInt("z", e.getBlockZ()); });
                sp.sendSystemMessage(Component.translatable("msg.madmodmash.wanted_seen", tag.getStringOr("name", "?"), e.getBlockX(), e.getBlockZ(),
                        (int) Math.sqrt(e.distanceToSqr(player))).withStyle(ChatFormatting.GOLD));
            } else sp.sendSystemMessage(Component.translatable("msg.madmodmash.wanted_lost", tag.getStringOr("name", "?"), tag.getIntOr("x", 0), tag.getIntOr("z", 0)).withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }
        EntityType<? extends Mob>[] pool = new EntityType[]{net.minecraft.world.entity.EntityTypes.ZOMBIE, net.minecraft.world.entity.EntityTypes.SKELETON, net.minecraft.world.entity.EntityTypes.PILLAGER, net.minecraft.world.entity.EntityTypes.VINDICATOR, ModEntities.ILLAGER_HERO.get(),
                ModEntities.MORTIS_ZOMBIE.get(), net.minecraft.world.entity.EntityTypes.WITCH, net.minecraft.world.entity.EntityTypes.HUSK, net.minecraft.world.entity.EntityTypes.STRAY, ModEntities.OVERWORLD_PIGLIN.get()};
        BlockPos pos = Util2.surface(sl, player.blockPosition(), 70, 140);
        Mob mob = Util2.spawn(sl, pool[player.getRandom().nextInt(pool.length)], pos);
        if (mob == null) return InteractionResult.FAIL;
        Champions.makeChampion(sl, mob, null);
        mob.getPersistentData().putBoolean(Champions.WANTED, true);
        mob.setPersistenceRequired();
        String name = mob.getCustomName() == null ? "?" : mob.getCustomName().getString();
        CustomData.update(DataComponents.CUSTOM_DATA, poster, t -> { t.putString("target", mob.getUUID().toString()); t.putString("name", name); t.putInt("x", pos.getX()); t.putInt("z", pos.getZ()); });
        poster.set(DataComponents.CUSTOM_NAME, Component.translatable("item.madmodmash.wanted_poster.named", name).withStyle(ChatFormatting.GOLD));
        sp.sendSystemMessage(Component.translatable("msg.madmodmash.wanted_new", name, pos.getX(), pos.getZ()).withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS;
    }
}
