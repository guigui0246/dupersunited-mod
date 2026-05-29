package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.features.SetHand;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class SetHandCommand extends Command {
    public SetHandCommand() {
        super("set-hand", "Sets your hand client side to any item/block.");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
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