package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.commands.MainCommand;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public final class NewCommandsCommand {
    private static final SimpleCommandExceptionType COULD_NOT_DUMP = new SimpleCommandExceptionType(new LiteralMessage("Could not dump commands, view logs for details."));

    private NewCommandsCommand() {}

    public static String getDescription() {
        return "Scans all server commands and dumps it into /DupersUnited/commands-dump.txt";
    }

    public static final SortedSet<String> commandList = new ObjectRBTreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("new-commands")
            .executes(ctx -> executeDump(ctx, "commands-dump"))
            .then(argument("filename", StringArgumentType.greedyString())
                .executes(ctx -> executeDump(ctx, StringArgumentType.getString(ctx, "filename"))));
    }

    public static void onCommandTree(RootCommandNode<ClientCommandSource> rootNode) {
        commandList.clear();

        rootNode.getChildren().stream()
            .filter(node -> node instanceof LiteralCommandNode<?>)
            .forEach(node -> commandList.add("/" + node.getName()));
    }

    private static int executeDump(CommandContext<FabricClientCommandSource> ctx, String outputFile) throws CommandSyntaxException {
        if (!Files.exists(SharedVariables.DIRECTORY) || !Files.isDirectory(SharedVariables.DIRECTORY)) {
            try {
                Files.createDirectories(SharedVariables.DIRECTORY);
            } catch (IOException e) {
                MainClient.LOGGER.error("Could not create directory", e);
                throw COULD_NOT_DUMP.create();
            }
        }

        Path file = SharedVariables.DIRECTORY.resolve(outputFile.endsWith(".txt") ? outputFile : outputFile + ".txt");

        try {
            Files.write(file, commandList);
        } catch (IOException e) {
            MainClient.LOGGER.error("Could not write dump file", e);
            throw COULD_NOT_DUMP.create();
        }

        if (!commandList.isEmpty()) {
            MainCommand.sendMessage("Successfully dumped " + commandList.size() + " commands!", true);
        } else {
            MainCommand.sendMessage("No commands captured yet odd...", true); // this will probably trigger if it's your first run of the command is hould maybe fix that
        }

        return 1;
    }
}