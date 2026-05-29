package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.crash.CrashReport;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.commands.MainCommand;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class KickCommand extends Command {
    public KickCommand() {
        super("kick", "Disconnects you from the server using various methods.");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(literal("disconnect")
                .executes(c -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.getNetworkHandler() == null) return 0;
                    client.execute(() ->
                        client.getNetworkHandler().getConnection()
                            .disconnect(Text.literal("Disconnected via kick command (/du kick disconnect)"))
                    );
                    return 1;
                })
            )
            .then(literal("pos")
                .executes(c -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientPlayerEntity player = client.player;
                    if (player == null || client.getNetworkHandler() == null) return 0;
                    client.execute(() ->
                        client.getNetworkHandler().sendPacket(
                            new PlayerMoveC2SPacket.PositionAndOnGround(
                                Double.NaN,
                                Double.NEGATIVE_INFINITY,
                                Double.POSITIVE_INFINITY,
                                !player.isOnGround(),
                                player.horizontalCollision
                            )
                        )
                    );
                    MainCommand.sendMessage(Text.literal("Sending invalid position packet...").formatted(Formatting.WHITE), true);
                    return 1;
                })
            )
            .then(literal("hurt")
                .executes(c -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientPlayerEntity player = client.player;
                    if (player == null || client.getNetworkHandler() == null) return 0;
                    client.execute(() -> player.setHealth(0f));
                    MainCommand.sendMessage(Text.literal("Sending invalid health packet...").formatted(Formatting.WHITE), true);
                    return 1;
                })
            )
            .then(literal("chat")
                .executes(c -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientPlayerEntity player = client.player;
                    if (player == null || client.getNetworkHandler() == null) return 0;
                    client.execute(() ->
                        player.networkHandler.sendChatMessage("§0§1§")
                    );
                    MainCommand.sendMessage(Text.literal("Sending malformed chat packet...").formatted(Formatting.WHITE), true);
                    return 1;
                })
            )
            .then(literal("crash")
                .executes(c -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    CrashReport report = CrashReport.create(new Throwable(), "Killed by DupersUnited crash command");

                    client.printCrashReport(report);

                    System.exit(1);
                    return 1;
                })
            );
    }
}