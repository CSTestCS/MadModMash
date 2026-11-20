package com.madmodmash.system;

import com.madmodmash.item.*;
import com.madmodmash.registry.*;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class PlayerEvents {
    static { NeoForge.EVENT_BUS.register(CallingEvents.class); }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        boolean first = !ModAttachments.get(p).entryDone;
        EntryEvents.run(p);
        if (first) p.sendSystemMessage(Component.translatable("msg.madmodmash.welcome").withStyle(ChatFormatting.GREEN));
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.isSpectator()) return;
        SetBonuses.fastTick(p);
        CallingEvents.fastTick(p);
        AbilityModuleItem.tickJetpack(p);
        boolean flight = p.hasEffect(ModEffects.BONFIRE_FLIGHT);
        if (!p.isCreative()) {
            if (flight && !p.getAbilities().mayfly) { p.getAbilities().mayfly = true; p.onUpdateAbilities(); p.getPersistentData().putBoolean("mm_bonfire_flight", true); }
            else if (!flight && p.getPersistentData().getBooleanOr("mm_bonfire_flight", false)) {
                p.getPersistentData().remove("mm_bonfire_flight");
                p.getAbilities().mayfly = false; p.getAbilities().flying = false; p.onUpdateAbilities();
            }
        }
        if (p.tickCount % 20 != 0) return;
        SetBonuses.tick(p);
        CallingEvents.tick(p);
        ItemStack off = p.getItemBySlot(EquipmentSlot.OFFHAND);
        if (off.getItem() instanceof RubyRingItem && p.tickCount % 60 == 0) p.heal(1);
        if (off.getItem() instanceof CharmItem charm) charm.tickHeld(p);
        if (p.tickCount % 40 == 0) Quests.poll(p);
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack s = event.getItemEntity().getItem();
        if (!(s.getItem() instanceof GildanItem)) return;
        Player p = event.getPlayer();
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack pouch = p.getInventory().getItem(i);
            if (pouch.getItem() instanceof CoinPouchItem) {
                CoinPouchItem.setCoins(pouch, CoinPouchItem.coins(pouch) + Economy.valueOf(s));
                event.getItemEntity().discard();
                p.take(event.getItemEntity(), s.getCount());
                event.setCanPickup(net.minecraft.util.TriState.FALSE);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        var adv = event.getAdvancement();
        var display = adv.value().display();
        if (display.isEmpty()) return;
        AdvancementType type = display.get().type();
        int tier = switch (type) { case TASK -> p.getRandom().nextInt(5); case GOAL -> 6 + p.getRandom().nextInt(5); case CHALLENGE -> 12 + p.getRandom().nextInt(8); };
        String path = adv.id().getPath();
        if (path.contains("kill_dragon") || path.contains("respawn_dragon")) tier = 21;
        else if (path.contains("netherite")) tier = 19;
        else if (path.contains("ancient_debris")) tier = 17;
        else if (path.contains("crying_obsidian")) tier = 16;
        else if (path.contains("diamond")) tier = Math.max(tier, 14);
        Engrams.spawn((ServerLevel) p.level(), p.position(), tier);
        Renown.add(p, type == AdvancementType.CHALLENGE ? 40 : type == AdvancementType.GOAL ? 20 : 8, "renown.madmodmash.advancement");
    }

    @SubscribeEvent
    public static void onXp(PlayerXpEvent.PickupXp event) {
        Player p = event.getEntity();
        int bonus = 0;
        if (SetBonuses.full(p, "amethyst")) bonus += Math.max(1, event.getOrb().getValue() / 2);
        if (p.getItemBySlot(EquipmentSlot.OFFHAND).getItem() instanceof CharmItem c && c.kind.equals("amethyst_charm")) bonus += Math.max(1, event.getOrb().getValue() / 2);
        if (bonus > 0) p.giveExperiencePoints(bonus);
    }

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(event.getAbstractVillager() instanceof Villager v)) return;
        var prof = v.getVillagerData().profession();
        if (prof.is(ModVillagers.CONTRACTOR.getKey()) || prof.is(ModVillagers.BOUNTY_BUYER.getKey())) Quests.onMeetContractor(p);
        if (prof.is(ModVillagers.CONTRACTOR.getKey()) || prof.is(ModVillagers.BOUNTY_BUYER.getKey()) || prof.is(ModVillagers.QUARTERMASTER.getKey()) || prof.is(ModVillagers.BANKER.getKey()))
            Renown.add(p, 2, "renown.madmodmash.trade");
    }

    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        if (event.getTamer() instanceof ServerPlayer p) {
            Quests.onTame(p, event.getAnimal());
            if (Calling.byName(ModAttachments.get(p).calling) == Calling.BEASTMASTER) {
                event.getAnimal().addEffect(new MobEffectInstance(MobEffects.STRENGTH, -1, 0));
                event.getAnimal().addEffect(new MobEffectInstance(MobEffects.RESISTANCE, -1, 0));
            }
        }
    }

    private PlayerEvents() {}
}
