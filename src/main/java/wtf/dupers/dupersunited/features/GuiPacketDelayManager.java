package wtf.dupers.dupersunited.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class GuiPacketDelayManager {
    private static boolean paused = false;
    private static boolean releasing = false;
    private static final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private static final Set<Class<? extends Packet<?>>> TARGET_PACKETS = Set.of(
            ButtonClickC2SPacket.class,
            ClickSlotC2SPacket.class
    );

    private GuiPacketDelayManager() {}

    public static boolean shouldPause(Packet<?> packet) {
        if (!paused || releasing) return false;

        return TARGET_PACKETS.contains(packet.getClass());
    }

    public static boolean isPaused() { return paused; }
    public static boolean isReleasing() { return releasing; }

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        paused = false;
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler != null) flush(handler);
    }

    public static void toggle() {
        if (paused) resume();
        else pause();
    }

    public static void queue(Packet<?> packet) {
        packetQueue.add(packet);
    }

    private static void flush(ClientPlayNetworkHandler handler) {
        releasing = true;
        Packet<?> packet;
        while ((packet = packetQueue.poll()) != null) {
            handler.sendPacket(packet);
        }
        releasing = false;
    }

    public static Queue<Packet<?>> getPacketQueue() { return packetQueue; }
    public static void clear() { packetQueue.clear(); }
}