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

/**
 * Hunt and gather contracts (Dark Adventures Hunt Contracts + RPG kill/gather bounties).
 * Kind 0 = blank (use to draw a contract), 1-3 = hunt tiers, 4 = gather.
 */
public class ContractItem extends Item {
    public final int kind;
    public ContractItem(Properties p, int kind) { super(p); this.kind = kind; }

    static final String[][] HUNT = {
        {},
        {"minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper", "madmodmash:ram", "madmodmash:boar", "madmodmash:stone_slime", "minecraft:drowned", "madmodmash:locust"},
        {"minecraft:witch", "minecraft:enderman", "minecraft:pillager", "madmodmash:illager_hero", "madmodmash:mortis_zombie", "madmodmash:iron_mimic", "madmodmash:zephyr", "minecraft:blaze", "minecraft:vindicator", "madmodmash:technomancer"},
        {"madmodmash:zombie_trudger", "madmodmash:headless_horseman", "minecraft:wither_skeleton", "minecraft:ravager", "minecraft:elder_guardian", "minecraft:evoker", "madmodmash:herobrine_cultist", "minecraft:piglin_brute"}
    };
    static final int[][] COUNTS = {{}, {6, 12}, {3, 6}, {1, 3}};
    static final String[] GATHER = {"madmodmash:thick_wool 8", "madmodmash:ram_horn 6", "madmodmash:wolf_fur 6", "madmodmash:creeper_leaves 6", "madmodmash:squid_skin 6",
            "madmodmash:steel_nugget 9", "madmodmash:bee_wing 4", "madmodmash:fox_pelt 3", "madmodmash:salmon_scale 8", "madmodmash:magma_scales 4",
            "madmodmash:endermite_mucus 4", "madmodmash:bat_fur 4", "minecraft:wheat 32", "minecraft:iron_ingot 16", "madmodmash:banana 8", "madmodmash:locust_wing 6"};

    public static CompoundTagView data(ItemStack s) { CustomData cd = s.get(DataComponents.CUSTOM_DATA); return new CompoundTagView(cd == null ? new net.minecraft.nbt.CompoundTag() : cd.copyTag()); }
    public record CompoundTagView(net.minecraft.nbt.CompoundTag tag) {
        public String target() { return tag.getStringOr("target", ""); }
        public int need() { return tag.getIntOr("need", 0); }
        public int have() { return tag.getIntOr("have", 0); }
    }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        RandomSource r = player.getRandom();
        if (kind == 0) {
            stack.shrink(1);
            ItemStack c = new ItemStack(r.nextInt(3) == 0 ? ModItems.GATHER_CONTRACT.get() : ModItems.COMMON_HUNT_CONTRACT.get());
            Util2.give(player, c);
            return InteractionResult.SUCCESS;
        }
        CompoundTagView d = data(stack);
        if (d.target().isEmpty()) { roll(stack, r); sp.sendSystemMessage(describe(stack).copy().withStyle(ChatFormatting.YELLOW)); return InteractionResult.SUCCESS; }
        if (kind == 4) {
            String[] parts = d.target().split(" ");
            ItemStack want = Util2.stack(parts[0], 1);
            int need = Integer.parseInt(parts[1]);
            int count = 0;
            for (ItemStack s : player.getInventory()) if (s.is(want.getItem())) count += s.getCount();
            if (count < need) { sp.sendSystemMessage(Component.translatable("msg.madmodmash.contract_gather_short", need - count, want.getHoverName()).withStyle(ChatFormatting.RED)); return InteractionResult.FAIL; }
            int left = need;
            for (int i = 0; i < player.getInventory().getContainerSize() && left > 0; i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(want.getItem())) { int t = Math.min(left, s.getCount()); s.shrink(t); left -= t; }
            }
            reward(sp, stack, 1);
            return InteractionResult.SUCCESS;
        }
        if (d.have() >= d.need()) { reward(sp, stack, kind); return InteractionResult.SUCCESS; }
        sp.sendSystemMessage(describe(stack).copy().withStyle(ChatFormatting.YELLOW));
        return InteractionResult.SUCCESS;
    }

    private void roll(ItemStack stack, RandomSource r) {
        if (kind == 4) { String g = GATHER[r.nextInt(GATHER.length)]; CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putString("target", g)); return; }
        String[] pool = HUNT[kind];
        String t = pool[r.nextInt(pool.length)];
        int n = COUNTS[kind][0] + r.nextInt(COUNTS[kind][1] - COUNTS[kind][0] + 1);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.putString("target", t); tag.putInt("need", n); tag.putInt("have", 0); });
    }

    private static void reward(ServerPlayer p, ItemStack stack, int tier) {
        stack.shrink(1);
        long coins = switch (tier) { case 1 -> 30 + p.getRandom().nextInt(40); case 2 -> 120 + p.getRandom().nextInt(120); case 3 -> 729 + p.getRandom().nextInt(400); default -> 20; };
        Economy.give(p, coins);
        Renown.add(p, tier == 3 ? 120 : tier == 2 ? 45 : 15, "renown.madmodmash.contract");
        if (tier == 3) Engrams.spawn((ServerLevel) p.level(), p.position(), 12 + p.getRandom().nextInt(6));
        if (tier == 2 && p.getRandom().nextBoolean()) Util2.give(p, new ItemStack(ModItems.IRON_KEY.get()));
        p.sendSystemMessage(Component.translatable("msg.madmodmash.contract_done", coins).withStyle(ChatFormatting.GREEN));
        p.level().playSound(null, p.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1F, 1F);
    }

    /** Called from CombatEvents for every kill by a player. */
    public static void onKill(ServerPlayer p, LivingEntity victim) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        for (ItemStack s : p.getInventory()) {
            if (!(s.getItem() instanceof ContractItem c) || c.kind < 1 || c.kind > 3) continue;
            CompoundTagView d = data(s);
            if (!d.target().equals(id) || d.have() >= d.need()) continue;
            int have = d.have() + 1;
            CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("have", have));
            p.sendOverlayMessage(Component.translatable("msg.madmodmash.contract_progress", have, d.need()).withStyle(ChatFormatting.YELLOW));
            break;
        }
    }

    public static Component describe(ItemStack stack) {
        CompoundTagView d = data(stack);
        if (d.target().isEmpty()) return Component.translatable("tooltip.madmodmash.contract_blank");
        if (stack.getItem() instanceof ContractItem c && c.kind == 4) {
            String[] parts = d.target().split(" ");
            return Component.translatable("tooltip.madmodmash.contract_gather", parts[1], Util2.stack(parts[0], 1).getHoverName());
        }
        var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse(d.target()));
        return Component.translatable("tooltip.madmodmash.contract_hunt", type.getDescription(), d.have(), d.need());
    }

    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, TooltipDisplay d, Consumer<Component> out, TooltipFlag f) {
        if (kind == 0) return;
        out.accept(describe(s).copy().withStyle(ChatFormatting.YELLOW));
    }
}
