package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import wtf.dupers.dupersunited.api.command.Command;

import static wtf.dupers.dupersunited.SharedVariables.randomQuote;

public final class QuoteCommand extends Command {
    public QuoteCommand() {
        super("quote", "Posts a random quote");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.executes(c -> {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(randomQuote());
            }
            return 1;
        });
    }
}