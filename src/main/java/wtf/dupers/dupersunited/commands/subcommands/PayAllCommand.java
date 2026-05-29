package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.PayAllManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PayAllCommand extends Command {
    public PayAllCommand() {
        super("pay-all", "Starts pay all macro");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(literal("start")
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