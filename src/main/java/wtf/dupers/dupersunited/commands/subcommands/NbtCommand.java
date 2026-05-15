package wtf.dupers.dupersunited.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.features.NBTEditor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public final class NbtCommand {
    private NbtCommand() {}

    public static String getDescription() {
        return "Shows you items NBT data.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("nbt")
                .executes(c -> {
                    var client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    var stack = client.player.getMainHandStack();
                    if (stack.isEmpty()) {
                        MainCommand.sendMessage("Hold an item first, can't edit your hand silly!", true);
                        return 0;
                    }

                    MainCommand.sendMessage("Opening NBT Editor...", true);
                    SharedVariables.screenToOpen = new NBTEditor(stack);
                    return 1;
                });
    }
}