package wtf.dupers.dupersunited.features;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.modules.glitcha.PayAllSettingsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.regex.Pattern;

public class PayAllManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Deque<String> queue = new ArrayDeque<>();
    private static final int MAX_PLAYER_NAME_LENGTH = 16;
    private static final int MIN_PLAYER_NAME_LENGTH = 3;
    private static boolean running = false;
    private static int tickCounter = 0;
    private static int TICK_DELAY;
    private static Pattern MROW_NYA = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
    private static Pattern NYA_MROW = Pattern.compile("^\\.[a-zA-Z0-9_]{2,16}$");
    private static final Set<String> HARDCODED_EXCLUSIONS = Set.of("*", "**", "***", "all", "everyone", "@a", "@p", "@r", "@s");

    private static boolean isValidPlayerName(String name) {
        if (name == null || name.isEmpty()) return false;
        String trimmed = name.trim();
        if (HARDCODED_EXCLUSIONS.contains(trimmed.toLowerCase(Locale.ROOT))) return false;

        boolean isGeyserName = trimmed.startsWith(".");
        int minLength = isGeyserName ? 4 : MIN_PLAYER_NAME_LENGTH;
        int maxLength = isGeyserName ? 17 : MAX_PLAYER_NAME_LENGTH;

        if (trimmed.length() < minLength || trimmed.length() > maxLength) return false;

        if (isGeyserName) {
            return NYA_MROW.matcher(trimmed).matches();
        }
        return MROW_NYA.matcher(trimmed).matches();
    }

    private static PayAllSettingsModule getSettings() {
        return MainClient.MODULE_MANAGER.getModule(PayAllSettingsModule.class);
    }

    public static Collection<PlayerListEntry> getPlayerList() {
        return mc.getNetworkHandler().getPlayerList();
    }

    public static void startPayAll() {
        if (mc.getNetworkHandler() == null) return;

        PayAllSettingsModule settings = getSettings();
        String rawAmount = settings.payAmount.getValue().trim();

        try {
            Double.parseDouble(rawAmount);
        } catch (NumberFormatException e) {
            MainCommand.sendMessage(Text.literal("Invalid amount: ").formatted(Formatting.WHITE)
                    .append(Text.literal(rawAmount).formatted(Formatting.AQUA))
                    .formatted(Formatting.WHITE), true);
            return;
        }

        TICK_DELAY = settings.delayBetweenPay.getValue();
        int playerCount = mc.getNetworkHandler().getPlayerList().size() - 1;

        String amount = rawAmount;
        if (settings.autoDivideAmt.getValue() && playerCount > 0) {
            try {
                double total = Double.parseDouble(rawAmount);
                amount = String.valueOf((int)(total / playerCount));
            } catch (NumberFormatException ignored) {}
        }

        queue.clear();
        for (PlayerListEntry entry : getPlayerList()) {
            String name = entry.getProfile().name();
            if (name == null || name.equals(mc.getSession().getUsername())) continue;
            if (!isValidPlayerName(name)) {
                //ChatLib.message("Couldn't pay" + name + " (name isn't supposed to be possible?)"); whatever it's technically fixed i see the issue but i don't wanna look into it more because that's work
                continue;
            }
            queue.add(name + ";" + amount);
        }

        MainCommand.sendMessage(Text.literal("Added ")
                .append(Text.literal(String.valueOf(queue.size())).formatted(Formatting.AQUA))
                .append(" players to the pay all queue.")
                .formatted(Formatting.WHITE), true);

        running = true;
        tickCounter = 0;
    }

    public static void stopPayAll() {
        running = false;
        queue.clear();
        tickCounter = 0;

        MainCommand.sendMessage(Text.literal("Payall ")
                .append(Text.literal("stopped").formatted(Formatting.AQUA))
                .append(".")
                .formatted(Formatting.WHITE), true);
    }

    public static void onTick() {
        if (!running || mc.player == null) return;

        tickCounter++;
        if (tickCounter < TICK_DELAY) return;
        tickCounter = 0;

        if (queue.isEmpty()) {
            running = false;
            return;
        }

        PayAllSettingsModule settings = getSettings();
        String[] entry = queue.poll().split(";", 3);
        String name = entry[0];
        String amount = entry.length > 1 ? entry[1] : "0";
        boolean isDouble = entry.length > 2 && entry[2].equals("double");

        String cmdTemplate = settings.commandSetting.getValue();
        String cmd = cmdTemplate
                .replace("<p>", name)
                .replace("<a>", amount)
                .replaceFirst("^/", "");

        mc.player.networkHandler.sendChatCommand(cmd);

        MainCommand.sendMessage(Text.literal("Sent ")
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(" ")
                .append(Text.literal(amount).formatted(Formatting.AQUA))
                .append(".")
                .formatted(Formatting.WHITE), true);

        if (!isDouble && settings.doubleSend.getValue()) {
            tickCounter = 0;
            queue.addFirst(name + ";" + amount + ";double");
        }
    }

    public static boolean isRunning() {
        return running;
    }
}