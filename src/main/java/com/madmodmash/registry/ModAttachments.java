package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import com.madmodmash.system.PlayerData;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MadModMash.MODID);

    public static final Supplier<AttachmentType<PlayerData>> PLAYER = ATTACHMENTS.register("player",
            () -> AttachmentType.builder(PlayerData::new).serialize(PlayerData.CODEC.fieldOf("data")).copyOnDeath().build());

    public static PlayerData get(Player player) {
        return player.getData(PLAYER);
    }

    private ModAttachments() {}
}
