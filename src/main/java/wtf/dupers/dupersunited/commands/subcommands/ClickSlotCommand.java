package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.ClickSlotManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ClickSlotCommand {
    private ClickSlotCommand() {}

    public static String getDescription() {
        return "Automatically clicks a specific inventory slot.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("click-slot")
                .executes(c -> {
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