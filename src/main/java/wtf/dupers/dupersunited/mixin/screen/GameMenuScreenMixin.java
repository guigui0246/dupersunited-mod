//package com.vinzy.dupersunited.mixin.screen;
//
//import net.minecraft.client.gl.RenderPipelines;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.gui.screen.GameMenuScreen;
//import net.minecraft.util.Identifier;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//
//@Mixin(GameMenuScreen.class)
//public class GameMenuScreenMixin {
//    @Unique
//    private static final Identifier LOGO = Identifier.of("dupersunited", "textures/meow/kitty.png");
//
//    @Inject(method = "render", at = @At("TAIL"))
//    private void dupersunited$render(DrawContext context, int mouseX, int mouseY, float deltaTicks,  CallbackInfo ci) {
//        int width = 128;
//        int height = 128;
//
//        int screenWidth = context.getScaledWindowWidth();
//        int screenHeight = context.getScaledWindowHeight();
//
//        int x = screenWidth - width;
//        int y = screenHeight / 2 - height / 2;
//
//        context.drawTexture(
//                RenderPipelines.GUI_TEXTURED,
//                LOGO,
//                x,
//                y,
//                0,
//                0,
//                width,
//                height,
//                width,
//                height
//                );
//    }
//}
