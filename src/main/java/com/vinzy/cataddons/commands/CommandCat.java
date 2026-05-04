package com.vinzy.cataddons.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.vinzy.cataddons.SharedVariables;
import com.vinzy.cataddons.commands.subcommands.*;
import com.vinzy.cataddons.features.screens.ClickGui;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class CommandCat {
    private static final Text FEEDBACK_PREFIX = Text.empty()
            .append(Text.literal("DU Public ").formatted(Formatting.BOLD, Formatting.AQUA))
            .append(Text.literal("» ").formatted(Formatting.DARK_GRAY));

    private CommandCat() {}

    public static void sendMessage(String message, boolean prefix) {
        sendMessage(Text.literal(message), prefix);
    }

    public static void sendMessage(Text message, boolean prefix) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            if (prefix) {
                player.sendMessage(Text.empty().append(FEEDBACK_PREFIX).append(message), false);
            } else {
                player.sendMessage(message, false);
            }
        }
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("du")
                            .executes(context -> {
                                SharedVariables.screenToOpen = new ClickGui(MinecraftClient.getInstance().currentScreen);
                                sendMessage("Opening ClickGUI!", true);
                                return 1;
                            })
                            .then(RestoreGhostsCommand.register())
                            .then(SetHandCommand.register(registryAccess))
                            .then(ToggleCommand.register())
                            .then(NbtCommand.register())
                            .then(ModuleCommand.register())
                            .then(ReloadConfigCommand.register())
                            .then(PluginsCommand.register())
                            .then(QuoteCommand.register())
                            .then(ReplaceBlockCommand.register(registryAccess))
                            .then(NewCommandsCommand.register())
                            .then(KeybindCommand.register())
                            .then(PayAllCommand.register())
                            .then(DupeCommand.register())
                            .then(ClickSlotCommand.register())
                            .then(HelpCommand.register())
                            .then(ForceOpCommand.register())
                            .then(DropCommand.register())
            );
        });
    }
}