package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.screens.mainmenu.KeybindScreen;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class KeybindCommand {
    private KeybindCommand() {}

    public static String getDescription() {
        return "Opens keybind GUI.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("keybinds")
                .executes(c -> {
                    var client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    MainCommand.sendMessage("Opening Keybinds Menu...", true);
                    SharedVariables.screenToOpen = new KeybindScreen(client.currentScreen);
                    return 1;
                });
    }
}