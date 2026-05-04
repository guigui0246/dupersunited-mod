package com.vinzy.cataddons.mixin.glitcha;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.exploit.AnySignModule;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public class AbstractSignEditScreenMixin {
    @Inject(method = "method_45658", at = @At("HEAD"), cancellable = true)
    private void cataddons$allowAnySignText(String text, CallbackInfoReturnable<Boolean> cir) {
        if (MainClient.MODULE_MANAGER.isEnabled(AnySignModule.class)) {
            cir.setReturnValue(true);
        }
    }
}