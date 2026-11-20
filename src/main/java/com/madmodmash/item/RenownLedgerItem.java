package com.madmodmash.item;

import com.madmodmash.*;
import com.madmodmash.entity.*;
import com.madmodmash.registry.*;
import com.madmodmash.system.*;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Shows rank, path, wrath and events. */
public class RenownLedgerItem extends Item {
    public RenownLedgerItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            ModCommands.status(sp.createCommandSourceStack(), sp);
            PlayerData d = ModAttachments.get(sp);
            int lvl = ImperialPath.level(d.renown);
            int next = (lvl + 1) * MashConfig.RENOWN_PER_PATH_LEVEL.get();
            if (lvl < 100) sp.sendSystemMessage(Component.translatable("msg.madmodmash.path_next", lvl + 1, next - d.renown, ImperialPath.preview(lvl, false),
                    d.charter ? ImperialPath.preview(lvl, true) : "-").withStyle(ChatFormatting.YELLOW));
            Wrath w = Wrath.get(sp.level().getServer());
            for (Faction f : Faction.values()) sp.sendSystemMessage(Component.translatable("msg.madmodmash.wrath_line", f.title, w.value(f), w.tier(f)).withStyle(f.color));
            for (String e : WorldEvents.get(sp.level().getServer()).describe()) sp.sendSystemMessage(Component.literal(" - " + e).withStyle(ChatFormatting.RED));
        }
        return InteractionResult.SUCCESS;
    }
}
