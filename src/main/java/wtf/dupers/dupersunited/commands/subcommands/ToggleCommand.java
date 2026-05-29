package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.api.command.arguments.ModuleArgumentType;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.commands.MainCommand;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles any module");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(argument("module", ModuleArgumentType.module())
            .executes(context -> {
                Module module = ModuleArgumentType.get(context);

                if (module == null) {
                    MainCommand.sendMessage(
                        Text.literal("Module not found.").formatted(Formatting.RED),
                        true
                    );

                    return 0;
                }

                module.toggle();

                Text status = module.isEnabled()
                    ? Text.literal("enabled").formatted(Formatting.GREEN)
                    : Text.literal("disabled").formatted(Formatting.RED);

                MainCommand.sendMessage(Text.empty()
                    .append(Text.literal(module.getName()).formatted(Formatting.AQUA))
                    .append(" is now ")
                    .append(status)
                    .append("."), true);

                return 1;
            })
        );
    }
}