package com.vinzy.cataddons.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.features.PluginScanner;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public final class PluginsCommand {
    private PluginsCommand() {}

    public static String getDescription() {
        return "Scans server for what plugins it has.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("plugins")
                .executes(c -> {
                    if (MinecraftClient.getInstance().player != null) {
                        PluginScanner.startScan();
                    } else {
                        CommandCat.sendMessage("wyd my glitcha!", false);
                    }
                    return 1;
                });
    }
}