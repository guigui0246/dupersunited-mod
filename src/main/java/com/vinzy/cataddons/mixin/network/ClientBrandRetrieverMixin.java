package com.vinzy.cataddons.mixin.network;

import com.vinzy.cataddons.features.ConfigManager;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientBrandRetriever.class, priority = 2000)
public class ClientBrandRetrieverMixin {

    @Inject(method = "getClientModName", at = @At("RETURN"), cancellable = true)
    private static void cataddons$spoofBrand(CallbackInfoReturnable<String> cir) {
        if (ConfigManager.brandSpoofEnabled) {
            cir.setReturnValue("vanilla");
        }
    }
}