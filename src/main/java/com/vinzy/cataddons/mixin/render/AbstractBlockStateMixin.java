package com.vinzy.cataddons.mixin.render;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.NoTextureRotationsModule;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Unique
    private static final long UNIFORM_RENDERING_SEED = 67L;

    @Inject(method = "getRenderingSeed", at = @At("HEAD"), cancellable = true)
    private void cataddons$uniformRenderingSeed(BlockPos pos, CallbackInfoReturnable<Long> cir) {
        NoTextureRotationsModule mod = MainClient.MODULE_MANAGER.getModule(NoTextureRotationsModule.class);
        if (mod != null && mod.isEnabled()) {
            cir.setReturnValue(UNIFORM_RENDERING_SEED);
        }
    }
}
