package wtf.dupers.dupersunited.mixin.network;

import wtf.dupers.dupersunited.features.GuiPacketDelayManager;
import wtf.dupers.dupersunited.features.PacketPauseManager;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {

    @Shadow
    @Final
    protected ClientConnection connection;

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void dupersunited$pausePackets(Packet<?> packet, CallbackInfo ci) {
        if (PacketPauseManager.isReleasing()) {
            this.connection.send(packet);
            ci.cancel();
            return;
        }

        if (PacketPauseManager.shouldPause(packet)) {
            PacketPauseManager.queue(packet);
            ci.cancel(); // prevent it from actually sending (shocked face emoji tone 2)
            return;
        }

        if (GuiPacketDelayManager.shouldPause(packet)) {
            GuiPacketDelayManager.queue(packet);
            ci.cancel();
            return;
        }

        if (GuiPacketDelayManager.isReleasing()) {
            this.connection.send(packet);
            ci.cancel();
            return;
        }

        this.connection.send(packet);
        ci.cancel();
    }
}