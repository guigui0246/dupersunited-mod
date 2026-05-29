package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.features.GhostBlock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class ReplaceBlockCommand extends Command {
    public ReplaceBlockCommand() {
        super("replace-block", "Places a client side block");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(argument("block", BlockStateArgumentType.blockState(registryAccess))
            .executes(context -> {
                BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);
                BlockState blockState = blockArg.getBlockState();
                GhostBlock.replaceBlock(blockState);
                return 1;
            })
        );
    }
}