package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import wtf.dupers.dupersunited.commands.MainCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class DropCommand {
    private DropCommand() {}

    public static String getDescription() {
        return "Drops the item in a specific inventory slot, or the currently held item if no slot is given.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("drop")
            .executes(c -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null || client.interactionManager == null) return 0;

                client.execute(() -> {
                    int hotbarIndex = client.player.getInventory().getSelectedSlot();
                    int screenSlot = 36 + hotbarIndex;

                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        screenSlot,
                        1,
                        SlotActionType.THROW,
                        client.player
                    );
                });

                int hotbarIndex = client.player.getInventory().getSelectedSlot();
                MainCommand.sendMessage(Text.literal("Dropped held item in hotbar slot ")
                    .append(Text.literal(String.valueOf(hotbarIndex)).formatted(Formatting.GREEN))
                    .append("."), true);
                return 1;
            })

            .then(argument("slot", IntegerArgumentType.integer(0, 90))
                .executes(c -> {
                    int slot = IntegerArgumentType.getInteger(c, "slot");
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null || client.interactionManager == null) return 0;
                    int screenSlot = slot < 9 ? 36 + slot : slot;

                    client.execute(() ->
                        client.interactionManager.clickSlot(
                            client.player.playerScreenHandler.syncId,
                            screenSlot,
                            1,
                            SlotActionType.THROW,
                            client.player
                        )
                    );

                    MainCommand.sendMessage(Text.literal("Dropped item in slot ")
                        .append(Text.literal(String.valueOf(slot)).formatted(Formatting.GREEN))
                        .append("."), true);
                    return 1;
                }));
    }
}