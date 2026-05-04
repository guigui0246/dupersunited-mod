package com.vinzy.cataddons.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.SharedVariables;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.features.screens.mainmenu.KeybindScreen;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class KeybindCommand {
    private KeybindCommand() {}

    public static String getDescription() {
        return "Opens keybind GUI.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("keybinds")
                .executes(c -> {
                    var client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    CommandCat.sendMessage("Opening Keybinds Menu...", true);
                    SharedVariables.screenToOpen = new KeybindScreen(client.currentScreen);
                    return 1;
                });
    }
}