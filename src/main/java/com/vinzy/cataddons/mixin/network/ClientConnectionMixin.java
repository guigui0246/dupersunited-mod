package com.vinzy.cataddons.mixin.network;

import com.vinzy.cataddons.features.PacketLogger;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.FreecamModule;
import com.vinzy.cataddons.modules.Module;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.vinzy.cataddons.features.SaveGuiManager.deadGui;
import static com.vinzy.cataddons.features.SaveGuiManager.savedScreen;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        for (Module module : MainClient.MODULE_MANAGER.getModules()) {
            if (module.isEnabled()) module.onPacketSend(packet);
        }
        
                 PacketLogger.log(packet, "OUT");

        if (packet instanceof CloseHandledScreenC2SPacket && savedScreen != null) {
            MinecraftClient.getInstance().execute(() -> {
                if (!deadGui) {
                    deadGui = true;
                    CommandCat.sendMessage(Text.literal("Your saved GUI was closed by the client.").formatted(Formatting.RED), true);
                }
            });
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"))
    private void onReceive(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        for (Module module : MainClient.MODULE_MANAGER.getModules()) {
            if (module.isEnabled()) module.onPacketRecieve(packet);
        }

        PacketLogger.log(packet, "IN");

        if (packet instanceof DeathMessageS2CPacket death) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && death.playerId() == mc.player.getId()) {
                mc.execute(() -> {
                    FreecamModule mod = MainClient.MODULE_MANAGER.getModule(FreecamModule.class);
                    if (mod != null && mod.isEnabled()) mod.setEnabled(false);
                });
            }
        }

        if (packet instanceof PlayerRespawnS2CPacket) {
            MinecraftClient.getInstance().execute(() -> {
                FreecamModule mod = MainClient.MODULE_MANAGER.getModule(FreecamModule.class);
                if (mod != null && mod.isEnabled()) mod.setEnabled(false);
            });
        }

        if (packet instanceof CloseScreenS2CPacket || packet instanceof OpenScreenS2CPacket) {
            MinecraftClient.getInstance().execute(() -> {
//                if (GuiMacro.isRecording) {
//                    GuiMacro.registerCloseGui();
//                }
                if (!deadGui && savedScreen != null) {
                    deadGui = true;
                    CommandCat.sendMessage(Text.literal("Your saved GUI was closed by the server.").formatted(Formatting.RED), true);
                }
            });
        }
    }
}
