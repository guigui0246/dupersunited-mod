package com.vinzy.cataddons.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.SharedVariables;
import com.vinzy.cataddons.features.NBTEditor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public final class NbtCommand {
    private NbtCommand() {}

    public static String getDescription() {
        return "Shows you items NBT data.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("nbt")
                .executes(c -> {
                    var client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    var stack = client.player.getMainHandStack();
                    if (stack.isEmpty()) {
                        CommandCat.sendMessage("Hold an item first, can't edit your hand silly!", true);
                        return 0;
                    }

                    CommandCat.sendMessage("Opening NBT Editor...", true);
                    SharedVariables.screenToOpen = new NBTEditor(stack);
                    return 1;
                });
    }
}