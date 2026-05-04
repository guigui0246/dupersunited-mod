package com.vinzy.cataddons.features;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.features.ssidLogin.SessionManager;
import com.vinzy.cataddons.utils.IClientCommandSource;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.*;

public class PluginScanner {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static boolean scanning = false;
    private static int completionId = -1;
    private static int ticksWaiting = 0;

    private static final Set<String> treePlugins = new ObjectRBTreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private static final SortedSet<String> foundPlugins = new ObjectRBTreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private static final Set<String> VERSION_ALIASES = Set.of(
        "version", "ver", "about", "bukkit:version", "bukkit:ver", "bukkit:about"
    );
    private static String versionAlias = null;

    private static final Set<String> ANTICHEAT_LIST = Set.of(
        "nocheatplus", "negativity", "warden", "horizon", "vulcan",
        "spartan", "grimac", "matrix", "kauri", "themis", "intave",
        "anticheat", "witherac", "godseye", "coreprotect", "wraith",
        "antixrayheuristics", "anticheatreloaded", "exploitsx",
        "foxaddition", "guardianac", "ggintegrity", "lightanticheat",
        "anarchyexploitfixes", "abc", "illegalstack", "polar"
    );

    public static void onCommandTree(RootCommandNode<ClientCommandSource> rootNode) {
        treePlugins.clear();
        versionAlias = null;

        rootNode.getChildren().stream().filter(node -> node instanceof LiteralCommandNode<?>).forEach(node -> {
            String name = node.getName();

            String[] split = name.split(":");
            if (split.length > 1) {
                treePlugins.add(split[0]);
            }

            if (versionAlias == null && VERSION_ALIASES.contains(name) && node.getChildren().stream().anyMatch(childNode -> childNode instanceof ArgumentCommandNode<?,?>)) {
                versionAlias = name;
            }
        });
    }

    public static void startScan() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;

        if (scanning) {
            CommandCat.sendMessage("Already scanning plugins, silly!", true);
            return;
        }

        foundPlugins.clear();
        scanning = true;
        completionId = ((IClientCommandSource) mc.getNetworkHandler().getCommandSource()).cataddons$beginCompletion();
        ticksWaiting = 0;

        CommandCat.sendMessage("Starting plugin scan...", true);

        String cmd = versionAlias != null ? versionAlias : "ver";
        mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(completionId, cmd + " "));
    }

    public static void onTick() {
        if (!scanning) return;

        if (++ticksWaiting >= 100) {
            printResults();
        }
    }

    public static void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
        if (scanning && packet.id() == completionId) {
            assert mc.getNetworkHandler() != null; // this function called from command handler

            ((IClientCommandSource) mc.getNetworkHandler().getCommandSource()).cataddons$endCompletion();

            var suggestions = packet.getSuggestions().getList();

            if (!suggestions.isEmpty()) {
                for (var s : suggestions) {
                    String name = s.getText().trim();
                    if (!name.isEmpty() && !name.equals(SessionManager.getUsername())) {
                        foundPlugins.add(name);
                    }
                }
            }

            printResults();
        }
    }

    private static void printResults() {
        scanning = false;
        completionId = -1;
        ticksWaiting = 0;
        if (mc.player == null) return;

        foundPlugins.addAll(treePlugins);

        if (foundPlugins.isEmpty()) {
            CommandCat.sendMessage(
                Text.literal("Could not find any plugins.").formatted(Formatting.RED),
                true
            );
            return;
        }

        MutableText text = Text.literal("Found ")
            .append(Text.literal(Integer.toString(foundPlugins.size())).formatted(Formatting.AQUA))
            .append(" plugins: ")
            .append(Texts.join(foundPlugins, Texts.DEFAULT_SEPARATOR_TEXT, plugin ->
                Text.literal(plugin).formatted(isAnticheat(plugin) ? Formatting.BLUE : Formatting.GREEN)));

        CommandCat.sendMessage(text, true);
    }

    private static boolean isAnticheat(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return ANTICHEAT_LIST.contains(n) || n.contains("exploit") || n.contains("anti") || n.contains("shield");
    }
}