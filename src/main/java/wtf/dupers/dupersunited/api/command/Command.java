package wtf.dupers.dupersunited.api.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.commands.MainCommand;

public abstract class Command {
    public final String command;
    public final String description;
    public String qualifiedName;

    public Command(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public Command(String command) {
        this(command, "No description provided.");
    }

    public abstract void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess);

    /* Utils */

    protected void sendMessage(String message, boolean prefix) {
        MainCommand.sendMessage(message, prefix);
    }

    protected void sendMessage(Text message, boolean prefix) {
        MainCommand.sendMessage(message, prefix);
    }

    protected void openScreen(Screen screen) {
        SharedVariables.screenToOpen = screen;
    }
}
