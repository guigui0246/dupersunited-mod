package com.vinzy.cataddons.features;

import net.minecraft.util.math.MathHelper;

public class TPSDisplay {
    public static long lastPacketTime = -1;
    public static double tps = 20.0;

    public static void onWorldTimeUpdate() {
        long now = System.currentTimeMillis();
        if (lastPacketTime != -1) {
            long delta = Math.max(1, now - lastPacketTime);
            double instantTps = 20000.0 / (double) delta;
            tps = MathHelper.clamp((tps * 0.8) + (Math.min(20.0, instantTps) * 0.2), 0, 20);
        }
        lastPacketTime = now;
    }

    public static String getTpsColorCode(double tps) {
        if (tps < 12.0) return "§c";
        if (tps < 17.0) return "§e";
        return "§a";
    }

}
