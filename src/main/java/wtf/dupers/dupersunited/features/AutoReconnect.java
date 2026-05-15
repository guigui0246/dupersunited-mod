package wtf.dupers.dupersunited.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class AutoReconnect {

    private static final int RECONNECT_DELAY_SECONDS = 5;

    private static int ticksRemaining = -1;
    private static String cachedAddress = null;
    private static ButtonWidget cancelButton = null;

    public static void cacheAddress(String address) {
        cachedAddress = address;
    //    MainClient.LOGGER.info("cached address {}", cachedAddress);
    }

    public static void startCountdown() {
        if (cachedAddress == null) {
       //     MainClient.LOGGER.warn("no cached server address, can't reconnect!!");
            return;
        }

        if (!ConfigManager.autoReconnectEnabled) {
        //    MainClient.LOGGER.info("autoreconnect is disabled, skipping");
            return;
        }

        ticksRemaining = RECONNECT_DELAY_SECONDS * 20;
    }

    public static void setCancelButton(ButtonWidget btn) {
        cancelButton = btn;
    }

    public static void cancel() {
        ticksRemaining = -1;

        if (cancelButton != null) {
            cancelButton.setMessage(Text.literal("§cReconnect Cancelled"));
            cancelButton.active = false;
            cancelButton = null;
        }
    }

    public static void tick() {
        if (ticksRemaining <= 0) return;

        ticksRemaining--;

        if (cancelButton != null && cancelButton.active) {
            cancelButton.setMessage(Text.literal("Reconnecting in " + getSecondsRemaining() + "s"));
        }

        if (ticksRemaining == 0) {
            ticksRemaining = -1;
            cancelButton = null;
            reconnect();
        }
    }

    public static int getSecondsRemaining() {
        return (int) Math.ceil(ticksRemaining / 20.0);
    }

    public static boolean isCountingDown() {
        return ticksRemaining > 0;
    }

    private static void reconnect() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (cachedAddress == null) {
         //   MainClient.LOGGER.warn("reconnect called but no cached address!");
            return;
        }

      //  MainClient.LOGGER.info("reconnecting to {}", cachedAddress);

        ServerInfo info = new ServerInfo("AutoReconnect", cachedAddress, ServerInfo.ServerType.OTHER);
        ServerAddress address = ServerAddress.parse(cachedAddress);

        ConnectScreen.connect(
                new MultiplayerScreen(new TitleScreen()),
                client,
                address,
                info,
                false,
                null
        );
    }
}