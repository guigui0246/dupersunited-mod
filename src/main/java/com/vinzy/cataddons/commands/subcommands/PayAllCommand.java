package com.vinzy.cataddons.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.features.PayAllManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PayAllCommand {
    private PayAllCommand() {}

    public static String getDescription() {
        return "Starts pay all macro";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("pay-all")
                .then(literal("start")
                        .executes(c -> {
                            if (PayAllManager.isRunning()) {
                                CommandCat.sendMessage(Text.literal("Payall ")
                                        .append(Text.literal("is already running").formatted(Formatting.WHITE))
                                        .append("."), true);
                                return 0;
                            }

                            PayAllManager.startPayAll();
                            CommandCat.sendMessage(Text.literal("Starting ")
                                    .append(Text.literal("PayAll").formatted(Formatting.AQUA))
                                    .append(" process...").formatted(Formatting.WHITE), true);
                            return 1;
                        }))
                
                .then(literal("stop")
                        .executes(c -> {
                            if (!PayAllManager.isRunning()) {
                                CommandCat.sendMessage(Text.literal("Payall ")
                                        .append(Text.literal("is not currently running").formatted(Formatting.WHITE))
                                        .append("."), true);
                                return 0;
                            }

                            PayAllManager.stopPayAll();
                            CommandCat.sendMessage(Text.literal("Stopped ")
                                    .append(Text.literal("PayAll").formatted(Formatting.AQUA))
                                    .append(" process.").formatted(Formatting.WHITE), true);
                            return 1;
                        }));
    }
}