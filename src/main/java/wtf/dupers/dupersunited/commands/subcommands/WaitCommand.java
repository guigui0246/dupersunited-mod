package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.commands.MainCommand;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class WaitCommand extends Command {
    public WaitCommand() {
        super("wait", "Waits a given number of milliseconds (tick-aligned), then optionally runs a command or sends a chat message.");
    }

    private record PendingWait(int ticksRemaining, String cmd) {}

    private static final List<PendingWait> pending = new ArrayList<>();

    public static void onTick() {
        if (pending.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Iterator<PendingWait> it = pending.iterator();

        while (it.hasNext()) {
            PendingWait wait = it.next();
            int remaining = wait.ticksRemaining() - 1;

            if (remaining <= 0) {
                it.remove();
                if (wait.cmd() != null && client.player != null) {
                    if (wait.cmd().startsWith("/")) {
                        client.player.networkHandler.sendChatCommand(wait.cmd().substring(1));
                    } else {
                        client.player.networkHandler.sendChatMessage(wait.cmd());
                    }
                } else if (wait.cmd() == null) {
                    MainCommand.sendMessage(
                        Text.literal("Done waiting!").formatted(Formatting.WHITE),
                        true
                    );
                }
            } else {
                it.remove();
                pending.add(new PendingWait(remaining, wait.cmd()));
                break;
            }
        }
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(argument("ms", IntegerArgumentType.integer(1))
            .executes(c -> {
                int ms = IntegerArgumentType.getInteger(c, "ms");
                int ticks = Math.max(1, ms / 50);

                MainCommand.sendMessage(
                    Text.literal("Waiting ")
                        .append(Text.literal(ms + "ms").formatted(Formatting.RED))
                        .append(Text.literal(" (" + ticks + " ticks)...").formatted(Formatting.GREEN)),
                    true
                );

                pending.add(new PendingWait(ticks, null));
                return 1;
            })

            .then(argument("cmd", StringArgumentType.greedyString())
                .executes(c -> {
                    int ms = IntegerArgumentType.getInteger(c, "ms");
                    String cmd = StringArgumentType.getString(c, "cmd");
                    int ticks = Math.max(1, ms / 50);

                    boolean isCommand = cmd.startsWith("/");

                    MainCommand.sendMessage(
                        Text.literal("Waiting ")
                            .append(Text.literal(ms + "ms").formatted(Formatting.RED))
                            .append(Text.literal(" " + ticks + " tick(s)...").formatted(Formatting.GREEN))
                            .append(Text.literal(isCommand ? " then running: " : " then saying: ").formatted(Formatting.WHITE))
                            .append(Text.literal(cmd).formatted(Formatting.AQUA)),
                        true
                    );

                    pending.add(new PendingWait(ticks, cmd));
                    return 1;
                })
            )
        );
    }
}