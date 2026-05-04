package com.vinzy.cataddons.features;

import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.keybinds.JoinServerInviteKeybind;
import com.vinzy.cataddons.utils.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

public final class ServerInviteManager {

    private static final long INVITE_DURATION_MS = 15_000L;

    private static volatile Invite activeInvite;

    private ServerInviteManager() {}

    public static void receiveInvite(String ip, String inviter, String sentAt) {
        if (ip == null || ip.isBlank() || inviter == null || inviter.isBlank()) return;
        if (isCurrentServer(ip)) return;
        activeInvite = new Invite(ip.trim(), inviter.trim(), sentAt, System.currentTimeMillis() + INVITE_DURATION_MS);
    }

    public static void joinActiveInvite() {
        Invite invite = getActiveInvite();
        if (invite == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            try {
                ServerInfo info = new ServerInfo("Server Invite", invite.ip(), ServerInfo.ServerType.OTHER);
                ServerAddress address = ServerAddress.parse(invite.ip());
                activeInvite = null;
                ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, address, info, false, null);
            } catch (Exception exception) {
                CommandCat.sendMessage(Text.literal("Failed to join invited server: ")
                        .append(Text.literal(exception.getMessage() == null ? "invalid address" : exception.getMessage()).formatted(Formatting.RED)), true);
            }
        });
    }

    public static void render(DrawContext context, MinecraftClient client) {
        Invite invite = getActiveInvite();
        if (invite == null) return;

        String keybindText = getJoinKeybindText();
        if (keybindText == null) return;

        String message = "§b§l" + invite.inviter() + "§r §fhas invited you to join §b§l" + invite.ip() + "§r§f, click §b§l" + keybindText + "§r§f to join!";

        int width = client.getWindow().getScaledWidth();
        int height = 22;
        int textWidth = client.textRenderer.getWidth(message);
        int x = Math.max(6, (width - textWidth) / 2);

        context.fill(0, 0, width, height, ColorUtil.FADED_INDIGO);
        context.fill(0, height - 1, width, height, ColorUtil.DEEP_INDIGO);
        context.drawText(client.textRenderer, message, x, 7, 0xFFFFFFFF, true);
    }

    private static Invite getActiveInvite() {
        Invite invite = activeInvite;
        if (invite == null) return null;
        if (isCurrentServer(invite.ip())) {
            activeInvite = null;
            return null;
        }
        if (System.currentTimeMillis() > invite.expiresAtMs()) {
            activeInvite = null;
            return null;
        }
        return invite;
    }

    private static String getJoinKeybindText() {
        int keyCode = JoinServerInviteKeybind.INSTANCE.getKeyCode();
        if (keyCode == -1) return null;
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) return null;
        return InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString().toUpperCase();
    }

    private static boolean isCurrentServer(String ip) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() == null) return false;

        String current = normalizeAddress(client.getCurrentServerEntry().address);
        String invited = normalizeAddress(ip);
        return current != null && current.equals(invited);
    }

    private static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) return null;
        try {
            ServerAddress parsed = ServerAddress.parse(address.trim());
            return (parsed.getAddress() + ":" + parsed.getPort()).toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            return address.trim().toLowerCase(Locale.ROOT);
        }
    }

    private record Invite(String ip, String inviter, String sentAt, long expiresAtMs) {}
}