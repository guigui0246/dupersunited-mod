package com.vinzy.cataddons.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.utils.ServerUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ForceOpCommand {
    private ForceOpCommand() {}

    public static String getDescription() {
        return "Does insane crazy force-op exploit on DonutSMP";
    }

    private static MinecraftClient mc = MinecraftClient.getInstance();
    public static Boolean amILarpingItUp = false;

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("force-op")
                .executes(c -> {
                    if (!ServerUtils.isDonut()) {
                        CommandCat.sendMessage("You must be on Donut SMP to use this command!", true);
                        return 1;
                    }

                    if (mc.player == null) return 0;
                    String playerName = mc.player.getName().getString();
                    mc.player.sendMessage(
                            Text.literal("[Server] Opped " + playerName).formatted(Formatting.GRAY, Formatting.ITALIC), false
                    );

                    amILarpingItUp = true;

                    Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                        mc.execute(() -> {
                            if (mc.player != null && mc.getNetworkHandler() != null) {
                                mc.getNetworkHandler().getConnection().disconnect(
                                        Text.empty()
                                                .append(Text.literal("You are temporarily banned for exploiting.\n\n")
                                                        .styled(s -> s.withColor(Formatting.RED)))
                                                .append(Text.literal("Time Left: ")
                                                        .styled(s -> s.withColor(Formatting.GRAY)))
                                                .append(Text.literal("359 day 23 hours 59 minutes\n\n")
                                                        .styled(s -> s.withColor(Formatting.WHITE)))
                                                .append(Text.literal("Ban ID: "))
                                                .styled(s -> s.withColor(Formatting.GRAY))
                                                .append(Text.literal("#1a507CoV\n")
                                                        .styled(s -> s.withColor(Formatting.WHITE)))
                                                .append(Text.literal("You may be able to appeal this ban on\n"))
                                                .styled(s -> s.withColor(Formatting.GRAY))
                                                .append(Text.literal("discord.gg/donutsmp\n\n")
                                                        .styled(s -> s.withColor(Formatting.WHITE)))
                                );
                            }
                        });
                    }, 5, TimeUnit.SECONDS);

                    return 1;
                });
    }
}