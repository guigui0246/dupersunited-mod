package wtf.dupers.dupersunited.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.features.screens.ClickGui;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public final class MainCommand {
    private static final Text FEEDBACK_PREFIX = Text.empty()
            .append(Text.literal("DupersUnited ").formatted(Formatting.BOLD, Formatting.AQUA))
            .append(Text.literal("» ").formatted(Formatting.DARK_GRAY));

    private MainCommand() {}

    public static void sendMessage(String message, boolean prefix) {
        sendMessage(Text.literal(message), prefix);
    }

    public static void sendMessage(Text message, boolean prefix) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            if (prefix) {
                player.sendMessage(Text.empty().append(FEEDBACK_PREFIX).append(message), false);
            } else {
                player.sendMessage(message, false);
            }
        }
    }

    public static void register(Map<String, Command> commands) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root = literal("du").executes(ctx -> {
                SharedVariables.screenToOpen = new ClickGui(MinecraftClient.getInstance().currentScreen);
                sendMessage("Opening ClickGUI!", true);
                return 1;
            });

            commands.forEach((subcommand, command) -> {
                LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(subcommand);
                command.build(builder, registryAccess);
                root.then(builder);
            });

            dispatcher.register(root);
        });
    }
}