package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import wtf.dupers.dupersunited.commands.MainCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class HelpCommand {

    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        DESCRIPTIONS.put("help", "Shows this help screen.");
        DESCRIPTIONS.put("restore-ghosts", RestoreGhostsCommand.getDescription());
        DESCRIPTIONS.put("set-hand", SetHandCommand.getDescription());
        DESCRIPTIONS.put("toggle", ToggleCommand.getDescription());
        DESCRIPTIONS.put("nbt", NbtCommand.getDescription());
        DESCRIPTIONS.put("module", ModuleCommand.getDescription());
        DESCRIPTIONS.put("reload-config", ReloadConfigCommand.getDescription());
        DESCRIPTIONS.put("plugins", PluginsCommand.getDescription());
        DESCRIPTIONS.put("quote", QuoteCommand.getDescription());
        DESCRIPTIONS.put("replace-block", ReplaceBlockCommand.getDescription());
        DESCRIPTIONS.put("new-commands", NewCommandsCommand.getDescription());
        DESCRIPTIONS.put("keybinds", KeybindCommand.getDescription());
        DESCRIPTIONS.put("pay-all", PayAllCommand.getDescription());
        DESCRIPTIONS.put("dupe", DupeCommand.getDescription());
        DESCRIPTIONS.put("force-op", ForceOpCommand.getDescription());
        DESCRIPTIONS.put("click-slot", ClickSlotCommand.getDescription());
        DESCRIPTIONS.put("drop", DropCommand.getDescription());
        DESCRIPTIONS.put("wait", WaitCommand.getDescription());
        DESCRIPTIONS.put("kick", KickCommand.getDescription());
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("help").executes(context -> {
            MainCommand.sendMessage(Text.literal("Listing all available commands:").formatted(Formatting.WHITE), true);

            CommandNode<FabricClientCommandSource> duNode = context.getRootNode().getChild("du");

            if (duNode != null) {
                for (CommandNode<FabricClientCommandSource> child : duNode.getChildren()) {
                    String name = child.getName();
                    // in case someone forgets to add description or if there's nothing for some reason, just throw this
                    String desc = DESCRIPTIONS.getOrDefault(name, "No description provided.");

                    MutableText text = Text.literal("/du " + name).formatted(Formatting.AQUA);
                    text.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY));
                    text.append(Text.literal(desc).formatted(Formatting.GRAY));

                    // swag thing to show what the command does
                    text.styled(style -> style
                            .withClickEvent(new ClickEvent.SuggestCommand("/du " + name + " "))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to show /du " + name + "!")))
                    );

                    MainCommand.sendMessage(text, false);
                }
            }

            return 1;
        });
    }
}