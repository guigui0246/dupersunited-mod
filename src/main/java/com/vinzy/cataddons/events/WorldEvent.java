package com.vinzy.cataddons.events;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.features.GhostBlock;
import com.vinzy.cataddons.features.GuiPacketDelayManager;
import com.vinzy.cataddons.features.PacketPauseManager;
import com.vinzy.cataddons.features.PayAllManager;
import com.vinzy.cataddons.modules.render.FreecamModule;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class WorldEvent {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (PacketPauseManager.isPaused()) {
                PacketPauseManager.toggle();
            }
            if (GuiPacketDelayManager.isPaused()) {
                GuiPacketDelayManager.resume();
            }
            GhostBlock.clearGhosts();
            FreecamModule mod = MainClient.MODULE_MANAGER.getModule(FreecamModule.class);
            if (mod != null && mod.isEnabled()) mod.setEnabled(false);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (PacketPauseManager.isPaused()) {
                PacketPauseManager.toggle();
            }
            if (GuiPacketDelayManager.isPaused()) {
                GuiPacketDelayManager.resume();
            }
            GhostBlock.clearGhosts();
            FreecamModule mod = MainClient.MODULE_MANAGER.getModule(FreecamModule.class);
            if (mod != null && mod.isEnabled()) mod.setEnabled(false);
            if (PayAllManager.isRunning()) {
                PayAllManager.stopPayAll();
            }
        });
    }
}