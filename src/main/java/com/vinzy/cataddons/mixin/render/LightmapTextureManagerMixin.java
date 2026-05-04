package com.vinzy.cataddons.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.FullBrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float getGamma(float original) {
        FullBrightModule mod = MainClient.MODULE_MANAGER.getModule(FullBrightModule.class);
        if (mod.isEnabled() && mod.mode.getValue().equals("Gamma")) {
            return FullBrightModule.gamma;
        }
        return original;
    }

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;ambientLight()F"))
    private float getAmbientLight(float original) {
        FullBrightModule mod = MainClient.MODULE_MANAGER.getModule(FullBrightModule.class);
        if (mod.isEnabled() && mod.mode.getValue().equals("Ambient")) {
            return FullBrightModule.ambient;
        }
        return original;
    }
}
