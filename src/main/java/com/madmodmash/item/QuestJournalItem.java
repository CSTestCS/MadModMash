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

/** The Unobtainables questlines. */
public class QuestJournalItem extends Item {
    public QuestJournalItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            for (Quests.Line l : Quests.Line.values()) {
                int st = Quests.stage(sp, l);
                String name = l.name().toLowerCase(java.util.Locale.ROOT);
                Component next = st >= 4 ? Component.translatable("msg.madmodmash.quest_done") : Component.translatable("quest.madmodmash." + name + "." + (st + 1));
                sp.sendSystemMessage(Component.translatable("msg.madmodmash.journal_line", Component.translatable("quest.madmodmash." + name), st, next).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
