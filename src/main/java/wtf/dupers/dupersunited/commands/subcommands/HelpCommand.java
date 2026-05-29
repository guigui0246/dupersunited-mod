package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.commands.MainCommand;

public final class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Shows this help screen.");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.executes(context -> {
            MainCommand.sendMessage(Text.literal("Listing all available commands:").formatted(Formatting.WHITE), true);

            MainClient.getCommands().forEach(command -> {
                MutableText text = Text.literal("/du " + command.command).formatted(Formatting.AQUA);
                text.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY));
                text.append(Text.literal(command.description).formatted(Formatting.GRAY));

                // swag thing to show what the command does
                text.styled(style -> style
                    .withClickEvent(new ClickEvent.SuggestCommand("/du " + command.command + " "))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to show /du " + command.command + "!")))
                );

                MainCommand.sendMessage(text, false);
            });

            return 1;
        });
    }
}