package wtf.dupers.dupersunited.mixin.misc;

import wtf.dupers.dupersunited.features.AutoReconnect;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void dupersunited$tick(CallbackInfo ci) {
        AutoReconnect.tick();
    }
}