package com.vinzy.cataddons.commands.subcommands;

import static com.vinzy.cataddons.SharedVariables.randomQuote;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public final class QuoteCommand {
    private QuoteCommand() {}

    public static String getDescription() {
        return "Posts a random quote";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("quote")
                .executes(c -> {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(randomQuote());
                    }
                    return 1;
                });
    }
}