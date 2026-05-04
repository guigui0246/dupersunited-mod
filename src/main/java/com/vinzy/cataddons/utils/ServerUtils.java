package com.vinzy.cataddons.utils;

import com.vinzy.cataddons.commands.CommandCat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;


public class ServerUtils {
    public static List<String> getScoreboardLines() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null) return List.of();

        Scoreboard scoreboard = mc.world.getScoreboard();
        if (scoreboard == null) return List.of();

        ScoreboardObjective obj =
                scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (obj == null) return List.of();

        return scoreboard.getScoreboardEntries(obj).stream()
                .map(entry ->
                        Team.decorateName(
                                scoreboard.getScoreHolderTeam(entry.owner()),
                                Text.literal(entry.owner())
                        ).getString()
                )
                .toList();
    }
    public static String stripFormatting(String s) {
        return s.replaceAll("§.", "");
    }

    public static boolean isDonut() {
        var scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        var objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective != null) {
            return objective.getDisplayName().getString().contains("§lD§lo§ln§lu§lt§l S§lM§lP");
        }
        return false;
    }
}
