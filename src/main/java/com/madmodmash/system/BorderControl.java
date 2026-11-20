package com.madmodmash.system;

import com.madmodmash.registry.ModAttachments;
import com.madmodmash.registry.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** RPG mod "Allow Border Control": the world border opens up as the realm's renown grows. */
public final class BorderControl {
    private static boolean dirty = true;

    public static void markDirty() {
        dirty = true;
    }

    public static void tick(MinecraftServer server) {
        if (!dirty || server.getTickCount() % 100 != 0) return;
        dirty = false;
        ServerLevel overworld = server.overworld();
        if (!overworld.getGameRules().get(ModGameRules.BORDER_CONTROL.get())) return;
        int total = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) total += ModAttachments.get(p).renown;
        double size = 256 + total * 0.5;
        if (Math.abs(overworld.getWorldBorder().getSize() - size) > 1) overworld.getWorldBorder().lerpSizeBetween(overworld.getWorldBorder().getSize(), size, 5000L, overworld.getGameTime());
    }

    private BorderControl() {}
}
