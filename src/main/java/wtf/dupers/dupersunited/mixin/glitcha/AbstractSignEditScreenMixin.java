package wtf.dupers.dupersunited.mixin.glitcha;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.exploit.AnySignModule;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public class AbstractSignEditScreenMixin {
    @Inject(method = "method_45658", at = @At("HEAD"), cancellable = true)
    private void dupersunited$allowAnySignText(String text, CallbackInfoReturnable<Boolean> cir) {
        if (MainClient.MODULE_MANAGER.isEnabled(AnySignModule.class)) {
            cir.setReturnValue(true);
        }
    }
}