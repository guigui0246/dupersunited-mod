package wtf.dupers.dupersunited.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.features.GhostBlock;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class RestoreGhostsCommand {
    private RestoreGhostsCommand() {}

    public static String getDescription() {
        return "Restores all ghost blocks placed";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("restore-ghosts")
                .executes(context -> {
                    GhostBlock.restoreGhosts();
                    return 1;
                });
    }
}

