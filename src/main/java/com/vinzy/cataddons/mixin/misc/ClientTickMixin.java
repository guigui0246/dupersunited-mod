package com.vinzy.cataddons.mixin.misc;

import com.vinzy.cataddons.features.AutoReconnect;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void cataddons$tick(CallbackInfo ci) {
        AutoReconnect.tick();
    }
}