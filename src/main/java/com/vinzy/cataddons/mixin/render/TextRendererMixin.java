package com.vinzy.cataddons.mixin.render;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.NoRenderModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.class)
public class TextRendererMixin {

    @Redirect(
            method = "getGlyph",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isObfuscated()Z")
    )
    private boolean cataddons$plainObfuscatedText(Style style) {
        NoRenderModule mod = MainClient.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (mod != null && mod.isEnabled() && mod.plainObfuscatedText.getValue()) {
            return false;
        }
        return style.isObfuscated();
    }
}
