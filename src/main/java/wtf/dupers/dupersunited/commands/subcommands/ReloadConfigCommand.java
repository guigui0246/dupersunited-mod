package wtf.dupers.dupersunited.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.ConfigManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class ReloadConfigCommand {
    private ReloadConfigCommand() {}

    public static String getDescription() {
        return "Reloads your config";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("reload-config")
                .executes(c -> {
                    ConfigManager.load().join();
                    MainCommand.sendMessage("Reloaded config.", true);
                    return 1;
                });
    }
}