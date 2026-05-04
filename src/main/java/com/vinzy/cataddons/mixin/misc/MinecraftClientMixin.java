package com.vinzy.cataddons.mixin.misc;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.features.ssidLogin.SessionManager;
import com.vinzy.cataddons.modules.render.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInput(CallbackInfo ci) {
        FreecamModule mod = MainClient.MODULE_MANAGER.getModule(FreecamModule.class);
        if (mod == null || !mod.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    @Inject(method = "getSession", at = @At("HEAD"), cancellable = true)
    private void onGetSession(CallbackInfoReturnable<Session> cir) {
        if (SessionManager.overrideSession) {
            cir.setReturnValue(SessionManager.currentSession);
        }
    }
}