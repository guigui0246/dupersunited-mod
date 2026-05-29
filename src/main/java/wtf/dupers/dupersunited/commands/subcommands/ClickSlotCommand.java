package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.ClickSlotManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class ClickSlotCommand extends Command {
    public ClickSlotCommand() {
        super("click-slot", "Automatically clicks a specific inventory slot.");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.executes(c -> {
                MainCommand.sendMessage(Text.literal("Usage: /du click-slot <slot> <count> <delayMs>").formatted(Formatting.RED), true);
                return 1;
            })

            .then(argument("slot", IntegerArgumentType.integer(0, 90))
                .then(argument("count", IntegerArgumentType.integer(1, 10000))
                    .then(argument("delayMs", IntegerArgumentType.integer(0, 10000))
                        .executes(c -> {
                            int slot = IntegerArgumentType.getInteger(c, "slot");
                            int count = IntegerArgumentType.getInteger(c, "count");
                            int delay = IntegerArgumentType.getInteger(c, "delayMs");

                            ClickSlotManager.start(slot, count, delay);
                            MainCommand.sendMessage(Text.literal("Clicking slot ")
                                .append(Text.literal(String.valueOf(slot)).formatted(Formatting.GREEN))
                                .append(" (x")
                                .append(Text.literal(String.valueOf(count)).formatted(Formatting.AQUA))
                                .append(")"), true);
                            return 1;
                        }))));
    }
}