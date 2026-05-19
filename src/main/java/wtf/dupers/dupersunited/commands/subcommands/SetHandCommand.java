package wtf.dupers.dupersunited.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.features.SetHand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.ItemStackArgument;

public final class SetHandCommand {
    private SetHandCommand() {}

    public static String getDescription() {
        return "Sets your hand client side to any item/block.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register(CommandRegistryAccess registryAccess) {
        return literal("set-hand")
                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                        .then(argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ItemStackArgument itemArg = context.getArgument("item", ItemStackArgument.class);
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    SetHand.setHand(itemArg.createStack(amount, false));
                                    return 1;
                                })
                        )
                );
    }
}