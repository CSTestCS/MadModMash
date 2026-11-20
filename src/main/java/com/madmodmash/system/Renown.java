package com.madmodmash.system;

import com.madmodmash.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Imperial Renown and ranks (the Attributes.txt title ladder). */
public final class Renown {
    public record Rank(String name, int threshold, ChatFormatting color) {}

    public static final Rank[] RANKS = {
            new Rank("Peasant", 0, ChatFormatting.GRAY), new Rank("Recruit", 50, ChatFormatting.WHITE),
            new Rank("Soldier", 150, ChatFormatting.GREEN), new Rank("Sergeant", 350, ChatFormatting.DARK_GREEN),
            new Rank("Knight", 700, ChatFormatting.AQUA), new Rank("Captain", 1200, ChatFormatting.DARK_AQUA),
            new Rank("Commander", 2000, ChatFormatting.BLUE), new Rank("General", 3200, ChatFormatting.LIGHT_PURPLE),
            new Rank("Lord", 5000, ChatFormatting.DARK_PURPLE), new Rank("Hero", 7500, ChatFormatting.GOLD),
            new Rank("Legend", 11000, ChatFormatting.YELLOW), new Rank("Emperor", 16000, ChatFormatting.DARK_RED)
    };

    public static Rank rank(int renown) {
        Rank r = RANKS[0];
        for (Rank k : RANKS) if (renown >= k.threshold) r = k;
        return r;
    }

    public static int rankIndex(int renown) {
        int idx = 0;
        for (int i = 0; i < RANKS.length; i++) if (renown >= RANKS[i].threshold) idx = i;
        return idx;
    }

    public static int get(ServerPlayer p) {
        return ModAttachments.get(p).renown;
    }

    public static void add(ServerPlayer player, int amount, String reason) {
        if (amount == 0) return;
        PlayerData d = ModAttachments.get(player);
        int before = d.renown;
        d.renown = Math.max(0, d.renown + amount);
        player.sendOverlayMessage(Component.translatable("msg.madmodmash.renown_gain", amount, Component.translatable(reason))
                .withStyle(ChatFormatting.GREEN));
        int oldIdx = rankIndex(before), newIdx = rankIndex(d.renown);
        if (newIdx > oldIdx) {
            Rank r = RANKS[newIdx];
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(r.name()).withStyle(r.color(), ChatFormatting.BOLD)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("msg.madmodmash.rank_up")));
            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (newIdx >= 4) Unlocks.grantRankPerks(player, newIdx);
        }
        ImperialPath.claimPending(player);
        BorderControl.markDirty();
    }

    private Renown() {}
}
