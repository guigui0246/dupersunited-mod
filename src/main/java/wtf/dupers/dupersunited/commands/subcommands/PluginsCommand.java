package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.PluginScanner;

public final class PluginsCommand extends Command {
    public PluginsCommand() {
        super("plugins", "Scans server for what plugins it has.");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.executes(c -> {
            if (MinecraftClient.getInstance().player != null) {
                PluginScanner.startScan();
            } else {
                MainCommand.sendMessage("wyd my glitcha!", false);
            }
            return 1;
        });
    }
}