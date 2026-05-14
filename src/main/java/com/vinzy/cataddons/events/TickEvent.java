package com.vinzy.cataddons.events;

import com.vinzy.cataddons.commands.subcommands.WaitCommand;
import com.vinzy.cataddons.features.PayAllManager;
import com.vinzy.cataddons.features.PluginScanner;
import com.vinzy.cataddons.features.auth.AuthManager;
import com.vinzy.cataddons.features.chatmacros.ChatMacroManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

import static com.vinzy.cataddons.MainClient.MODULE_MANAGER;
import static com.vinzy.cataddons.SharedVariables.screenToOpen;

public class TickEvent {
    public static int pendingDisconnectTicks = -1;
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (pendingDisconnectTicks > 0) {
                pendingDisconnectTicks--;
            } else if (pendingDisconnectTicks == 0) {
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().getConnection()
                            .disconnect(Text.literal("Disconnected & sent packets"));
                }
                pendingDisconnectTicks = -1;
            }

            if (screenToOpen != null) {
                client.setScreen(screenToOpen);
                screenToOpen = null;
            }
            if (MODULE_MANAGER != null) {
                MODULE_MANAGER.onTick();
            }

            PluginScanner.onTick();
            PayAllManager.onTick();
            AuthManager.onTick();
            WaitCommand.onTick();
            ChatMacroManager.onTick();
        });
    }
}
