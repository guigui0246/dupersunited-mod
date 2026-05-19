package wtf.dupers.dupersunited.features;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;

public class ClickSlotManager {
    private static int slot = 0;
    private static int remaining = 0;
    private static long delay = 50;
    private static long nextClickTime = 0;
    private static boolean running = false;

    public static void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!running || remaining <= 0 || client.player == null) {
            running = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        ScreenHandler handler = (client.player.currentScreenHandler != null)
            ? client.player.currentScreenHandler
            : client.player.playerScreenHandler;

        if (slot < 0 || slot >= handler.slots.size()) {
            running = false;
            return;
        }

        ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
            handler.syncId,
            handler.getRevision(),
            (short) slot,
            (byte) 0,
            SlotActionType.PICKUP,
            new Int2ObjectArrayMap<>(),
            ItemStackHash.EMPTY
        );

        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(packet);
        }

        remaining--;
        nextClickTime = now + delay;
    }

    public static void start(int s, int count, int d) {
        slot = s;
        remaining = count;
        delay = d;
        nextClickTime = System.currentTimeMillis();
        running = true;
    }

    public static void stop() {
        running = false;
    }

    public static boolean isRunning() {
        return running;
    }
}