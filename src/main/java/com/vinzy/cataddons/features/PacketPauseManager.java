package com.vinzy.cataddons.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PacketPauseManager {
    private static boolean paused = false;
    private static boolean releasing = false;
    private static final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private static final Set<Class<? extends Packet<?>>> TARGET_PACKETS = ConcurrentHashMap.newKeySet();

    private PacketPauseManager() {}

    public static void addTarget(Class<? extends Packet<?>> packetClass) { TARGET_PACKETS.add(packetClass); }
    public static void clearTargets() { TARGET_PACKETS.clear(); }

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