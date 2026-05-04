package com.vinzy.cataddons.mixin.misc;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.misc.BetterTabModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.playerListKey.isPressed()) {
            BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
            if (module != null && module.isEnabled()) {
                module.onMouseScroll(vertical);
                ci.cancel(); // whatever this is surely not gonna be an issue haha if it is then idfk vinzys fault maybe
            }
        }
    }
}