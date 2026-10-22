package com.rnmd.cursedquest;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CursedQuest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CursedCommands {
    private CursedCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cq")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    QuestManager.showCurrentQuest(player);
                    return 1;
                })
                .then(Commands.literal("reset")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            QuestManager.reset(player);
                            return 1;
                        }))
                .then(Commands.literal("stage")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, QuestManager.MAX_STAGE))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    QuestManager.setStage(player, IntegerArgumentType.getInteger(context, "value"));
                                    return 1;
                                })))
                .then(Commands.literal("scare")
                        .then(Commands.argument("type", IntegerArgumentType.integer(0, 11))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int type = IntegerArgumentType.getInteger(context, "type");
                                    ModNetworking.sendScare(player, type);
                                    CursedEvents.playScareAudio(player, type);
                                    player.sendSystemMessage(Component.literal("Тест скримера " + type + "/11. Это был тест. Наверное.")
                                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
                                    return 1;
                                })))
                .then(Commands.literal("help")
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "/cq — задание; /cq reset — сброс; /cq stage <0-12> — этап (оператор); /cq scare <0-11> — тест экрана")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
                            return 1;
                        }))
        );
    }
}
