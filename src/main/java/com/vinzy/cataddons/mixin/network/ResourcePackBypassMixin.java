package com.vinzy.cataddons.mixin.network;

import com.vinzy.cataddons.features.ConfigManager;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ResourcePackBypassMixin {

    @Inject(method = "onResourcePackSend", at = @At("HEAD"), cancellable = true)
    private void cataddons$onResourcePackSend(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (!ConfigManager.rpBypassEnabled) return;

        ClientCommonNetworkHandler handler = (ClientCommonNetworkHandler) (Object) this;
        ResourcePackStatusC2SPacket.Status status = ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED;
        handler.sendPacket(new ResourcePackStatusC2SPacket(packet.id(), status));
        ci.cancel();
    }
}