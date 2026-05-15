package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.PayAllManager;
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
                                MainCommand.sendMessage(Text.literal("Payall ")
                                        .append(Text.literal("is already running").formatted(Formatting.WHITE))
                                        .append("."), true);
                                return 0;
                            }

                            PayAllManager.startPayAll();
                            MainCommand.sendMessage(Text.literal("Starting ")
                                    .append(Text.literal("PayAll").formatted(Formatting.AQUA))
                                    .append(" process...").formatted(Formatting.WHITE), true);
                            return 1;
                        }))

                .then(literal("stop")
                        .executes(c -> {
                            if (!PayAllManager.isRunning()) {
                                MainCommand.sendMessage(Text.literal("Payall ")
                                        .append(Text.literal("is not currently running").formatted(Formatting.WHITE))
                                        .append("."), true);
                                return 0;
                            }

                            PayAllManager.stopPayAll();
                            MainCommand.sendMessage(Text.literal("Stopped ")
                                    .append(Text.literal("PayAll").formatted(Formatting.AQUA))
                                    .append(" process.").formatted(Formatting.WHITE), true);
                            return 1;
                        }));
    }
}