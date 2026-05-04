package com.vinzy.cataddons.mixin.render;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.NoRenderModule;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SubmittableBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "addToBatch", at = @At("HEAD"), cancellable = true)
    private void cataddons$skipParticlesWhenDisabled(
            SubmittableBatch batch,
            Frustum frustum,
            Camera camera,
            float tickDelta,
            CallbackInfo ci
    ) {
        NoRenderModule mod = MainClient.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (mod == null || !mod.isEnabled()) {
            return;
        }
        if (!mod.particles.getValue()) {
            return;
        }
        ci.cancel();
    }
}
