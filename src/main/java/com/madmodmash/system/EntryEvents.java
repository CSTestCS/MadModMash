package com.madmodmash.system;

import com.madmodmash.entity.Bonfire;
import com.madmodmash.registry.ModAttachments;
import com.madmodmash.registry.ModEntities;
import com.madmodmash.registry.ModGameRules;
import com.madmodmash.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** "Entry Events" from the Dark Adventures notes: how each new player arrives in the world. */
public final class EntryEvents {
    public static void run(ServerPlayer p) {
        PlayerData d = ModAttachments.get(p);
        if (d.entryDone) return;
        d.entryDone = true;
        ServerLevel level = (ServerLevel) p.level();
        if (!level.getGameRules().get(ModGameRules.ENTRY_EVENTS.get())) return;
        Util2.give(p, new ItemStack(ModItems.RENOWN_LEDGER.get()));
        Util2.give(p, new ItemStack(ModItems.QUEST_JOURNAL.get()));
        dress(p);
        boolean beach = level.getBiome(p.blockPosition()).is(BiomeTags.IS_BEACH) || level.getBiome(p.blockPosition()).is(BiomeTags.IS_OCEAN);
        int roll = beach ? 0 : 1 + p.getRandom().nextInt(3);
        switch (roll) {
            case 0 -> shipwrecked(p, level);
            case 1 -> knockedOut(p);
            case 2 -> camping(p, level);
            default -> captive(p, level);
        }
    }

    private static void dress(ServerPlayer p) {
        p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.TATTERED_CHESTPLATE.get()));
        p.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.TATTERED_LEGGINGS.get()));
        p.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.TATTERED_BOOTS.get()));
    }

    private static void say(ServerPlayer p, String key) {
        p.sendSystemMessage(Component.translatable("entry.madmodmash." + key).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }

    private static void shipwrecked(ServerPlayer p, ServerLevel level) {
        say(p, "shipwrecked");
        BlockPos c = p.blockPosition();
        var r = p.getRandom();
        for (int i = 0; i < 8; i++) {
            BlockPos d = Util2.surface(level, c, 2, 7);
            level.setBlockAndUpdate(d, r.nextBoolean() ? Blocks.OAK_PLANKS.defaultBlockState() : Blocks.OAK_SLAB.defaultBlockState());
        }
        BlockPos chest = Util2.surface(level, c, 2, 4);
        level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, net.minecraft.core.Direction.NORTH));
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity be) {
            be.setItem(0, new ItemStack(Items.BREAD, 6));
            be.setItem(1, new ItemStack(ModItems.WOODEN_KNIFE.get()));
            be.setItem(2, new ItemStack(ModItems.COPPER_GILDAN.get(), 12));
            be.setItem(3, new ItemStack(ModItems.FABRIC.get(), 4));
            be.setItem(4, new ItemStack(Items.TORCH, 8));
        }
        if (r.nextBoolean()) {
            var crew = Util2.spawn(level, ModEntities.COMPANION.get(), Util2.surface(level, c, 2, 5));
            if (crew != null) crew.tame(p);
        }
    }

    private static void knockedOut(ServerPlayer p) {
        say(p, "knocked_out");
        p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200));
        p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 1));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60));
        Util2.give(p, new ItemStack(ModItems.ROCK.get(), 4));
    }

    private static void camping(ServerPlayer p, ServerLevel level) {
        say(p, "camping");
        BlockPos spot = Util2.surface(level, p.blockPosition(), 2, 3);
        Bonfire fire = Util2.spawn(level, ModEntities.BONFIRE.get(), spot);
        if (fire != null) fire.setOwner(p);
        Util2.give(p, new ItemStack(ModItems.WOODEN_CLUB.get()));
        Util2.give(p, new ItemStack(Items.BREAD, 8));
        Util2.give(p, new ItemStack(Items.TORCH, 16));
        Util2.give(p, new ItemStack(ModItems.RECALL_TOKEN.get(), 2));
        Util2.give(p, new ItemStack(Items.WOOL.white(), 6));
    }

    private static void captive(ServerPlayer p, ServerLevel level) {
        say(p, "captive");
        BlockPos c = p.blockPosition();
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) continue;
            for (int y = 0; y < 2; y++) {
                BlockPos b = c.offset(dx, y, dz);
                if (level.getBlockState(b).canBeReplaced())
                    level.setBlockAndUpdate(b, (dx == 0 && dz == 1 && y == 0) ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState());
            }
        }
        for (int i = 0; i < 2; i++) {
            var pil = Util2.spawn(level, net.minecraft.world.entity.EntityTypes.PILLAGER, Util2.surface(level, c, 10, 16));
            if (pil != null) pil.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        }
        Util2.give(p, new ItemStack(ModItems.LOCK_PICK.get(), 2));
    }

    private EntryEvents() {}
}
