package wtf.dupers.dupersunited.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.utils.ServerUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class DupeCommand {
    private DupeCommand() {}

    public static String getDescription() {
        return "Automatically dupes on DonutSMP";
    }

    private static MinecraftClient mc = MinecraftClient.getInstance();
    public static Boolean amILarpingItUp = false;

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("dupe")
                .executes(c -> {
                    if (!ServerUtils.isDonut()) {
                        MainCommand.sendMessage("You must be on Donut SMP to use this command!", true);
                        return 1;
                    }

                    mc.execute(() ->
                            mc.player.networkHandler.getConnection().disconnect(
                                    Text.empty()
                                            .append(Text.literal("You are temporarily banned for duping.\n\n")
                                                    .styled(s -> s.withColor(Formatting.RED)))
                                            .append(Text.literal("Time Left: ")
                                                    .styled(s -> s.withColor(Formatting.GRAY)))
                                            .append(Text.literal("13 day 23 hours 59 minutes\n\n")
                                                    .styled(s -> s.withColor(Formatting.WHITE)))
                                            .append(Text.literal("Ban ID: "))
                                            .styled(s -> s.withColor(Formatting.GRAY))
                                            .append(Text.literal("#1a507CoV\n")
                                                    .styled(s -> s.withColor(Formatting.WHITE)))
                                            .append(Text.literal("You may be able to appeal this ban on\n"))
                                            .styled(s -> s.withColor(Formatting.GRAY))
                                            .append(Text.literal("discord.gg/donutsmp\n\n")
                                                    .styled(s -> s.withColor(Formatting.WHITE)))
                            )
                    );

                    amILarpingItUp = true;

                    return 1;
                });
    }
}