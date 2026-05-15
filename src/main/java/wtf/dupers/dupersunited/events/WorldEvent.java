package wtf.dupers.dupersunited.events;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.features.GhostBlock;
import wtf.dupers.dupersunited.features.GuiPacketDelayManager;
import wtf.dupers.dupersunited.features.PacketPauseManager;
import wtf.dupers.dupersunited.features.PayAllManager;
import wtf.dupers.dupersunited.modules.render.FreecamModule;
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