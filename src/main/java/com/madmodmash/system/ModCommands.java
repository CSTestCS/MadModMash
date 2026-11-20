package com.madmodmash.system;

import com.madmodmash.registry.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /mash renown|wrath|event|calling - status and admin tools. */
public final class ModCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("mash")
                .then(Commands.literal("status").executes(c -> status(c.getSource(), c.getSource().getPlayerOrException())))
                .then(Commands.literal("renown").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(c -> { Renown.add(EntityArgument.getPlayer(c, "player"), IntegerArgumentType.getInteger(c, "amount"), "renown.madmodmash.command"); return 1; }))))
                .then(Commands.literal("wrath")
                        .executes(c -> { Wrath w = Wrath.get(c.getSource().getServer());
                            for (Faction f : Faction.values()) c.getSource().sendSuccess(() -> Component.literal(f.title + ": " + w.value(f) + " (tier " + w.tier(f) + ")").withStyle(f.color), false);
                            return 1; })
                        .then(Commands.argument("faction", StringArgumentType.word()).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Faction.values()).map(f -> f.name().toLowerCase(Locale.ROOT)), b))
                                .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(c -> {
                                    Faction f = Faction.valueOf(StringArgumentType.getString(c, "faction").toUpperCase(Locale.ROOT));
                                    Wrath.get(c.getSource().getServer()).add(c.getSource().getServer(), f, IntegerArgumentType.getInteger(c, "amount"));
                                    return 1; }))))
                .then(Commands.literal("event").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("list").executes(c -> {
                            var list = WorldEvents.get(c.getSource().getServer()).describe();
                            c.getSource().sendSuccess(() -> Component.literal(list.isEmpty() ? "No active events" : String.join("\n", list)), false);
                            return list.size(); }))
                        .then(Commands.literal("start").then(Commands.argument("type", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(WorldEvents.Type.values()).map(t -> t.name().toLowerCase(Locale.ROOT)), b))
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    WorldEvents.Type t = WorldEvents.Type.valueOf(StringArgumentType.getString(c, "type").toUpperCase(Locale.ROOT));
                                    WorldEvents.get(c.getSource().getServer()).start(c.getSource().getServer().overworld(), t, p.blockPosition());
                                    return 1; }))))
                .then(Commands.literal("calling").then(Commands.argument("calling", StringArgumentType.word())
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Calling.values()).map(t -> t.name().toLowerCase(Locale.ROOT)), b))
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            ModAttachments.get(p).calling = StringArgumentType.getString(c, "calling").toUpperCase(Locale.ROOT);
                            return 1; }))));
    }

    public static int status(CommandSourceStack src, ServerPlayer p) {
        PlayerData d = ModAttachments.get(p);
        Renown.Rank r = Renown.rank(d.renown);
        src.sendSuccess(() -> Component.translatable("msg.madmodmash.status_rank", Component.literal(r.name()).withStyle(r.color()), d.renown,
                ImperialPath.level(d.renown), d.charter ? Component.translatable("msg.madmodmash.charter_yes") : Component.translatable("msg.madmodmash.charter_no")), false);
        Calling c = Calling.byName(d.calling);
        src.sendSuccess(() -> Component.translatable("msg.madmodmash.status_calling", c == null ? Component.translatable("msg.madmodmash.none") : Component.translatable(c.key())), false);
        src.sendSuccess(() -> Component.translatable("msg.madmodmash.status_bank", d.bank).withStyle(ChatFormatting.GOLD), false);
        for (Quests.Line l : Quests.Line.values())
            src.sendSuccess(() -> Component.translatable("msg.madmodmash.status_quest", Component.translatable("quest.madmodmash." + l.name().toLowerCase(Locale.ROOT)),
                    Quests.stage(p, l)).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    private ModCommands() {}
}
