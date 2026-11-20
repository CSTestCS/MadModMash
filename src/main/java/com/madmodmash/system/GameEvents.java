package com.madmodmash.system;

import com.madmodmash.entity.Herobrine;
import com.madmodmash.registry.ModEntities;
import com.madmodmash.registry.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class GameEvents {
    /** Herobrine haunting: once the gamerule is on he stalks the night until it is switched off again. */
    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server.getTickCount() % 600 != 0) return;
        ServerLevel level = server.overworld();
        if (!level.getGameRules().get(ModGameRules.HEROBRINE_HAUNTING.get()) || level.isBrightOutside()) return;
        for (ServerPlayer p : level.players()) {
            if (p.getRandom().nextInt(6) != 0 || SetBonuses.full(p, "herobrine")) continue;
            if (!level.getEntitiesOfClass(Herobrine.class, p.getBoundingBox().inflate(128)).isEmpty()) continue;
            Util2.spawn(level, ModEntities.HEROBRINE.get(), Util2.surface(level, p.blockPosition(), 20, 32));
        }
    }

    /** Lore lines for every MadModMash item, and Trait info on any weapon. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack s = event.getItemStack();
        Trait t = Trait.of(s);
        if (t != null) event.getToolTip().add(1, net.minecraft.network.chat.Component.translatable("tooltip.madmodmash.trait",
                net.minecraft.network.chat.Component.translatable("trait.madmodmash." + t.name().toLowerCase(java.util.Locale.ROOT)),
                net.minecraft.network.chat.Component.translatable("enchantment.level." + Trait.level(s))).withStyle(t.color));
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem());
        if (!id.getNamespace().equals(com.madmodmash.MadModMash.MODID)) return;
        String key = s.getItem().getDescriptionId() + ".desc";
        if (net.minecraft.locale.Language.getInstance().has(key))
            event.getToolTip().add(net.minecraft.network.chat.Component.translatable(key).withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
    }

    private GameEvents() {}
}
