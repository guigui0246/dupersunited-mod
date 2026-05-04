package com.vinzy.cataddons.features;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.glitcha.TpsCounterModule;
import com.vinzy.cataddons.modules.render.HudModule;
import com.vinzy.cataddons.modules.render.WatermarkModule;
import com.vinzy.cataddons.features.screens.hud.HudEditorScreen;
import com.vinzy.cataddons.features.screens.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import static com.vinzy.cataddons.features.TPSDisplay.*;

public final class HudOverlay {

    private HudOverlay() {}


    private static void drawScaled(DrawContext ctx, MinecraftClient client, HudElement el, Runnable draw) {
        int screenWidth = client.getWindow().getScaledWidth();
        int actualX = el.getScreenX(screenWidth);

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate((float) actualX, (float) el.y);
        m.scale(el.scale, el.scale);
        draw.run();
        m.popMatrix();
    }

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            ServerInviteManager.render(drawContext, client);

            WatermarkModule watermarkMod = MainClient.MODULE_MANAGER.getModule(WatermarkModule.class);
            if (watermarkMod != null && watermarkMod.isEnabled()) {
                drawScaled(drawContext, client, HudEditorScreen.WATERMARK, () -> {
                    String text = watermarkMod.watermarkText.getValue().replace("&", "§");
                    int xOff = HudEditorScreen.WATERMARK.rightAligned ? HudEditorScreen.WATERMARK.getW() - client.textRenderer.getWidth(text) : 0;
                    drawContext.drawText(client.textRenderer, text, xOff, 0, 0xFFFFFFFF, true);
                });
            }

//            if (MacroManager.isRunning()) {
//                drawScaled(drawContext, client, HudEditorScreen.MACRO, () -> {
//                    String l1 = "§dActive GUI Macro";
//                    String l2 = "§8> §5" + MacroManager.getRunningName();
//                    int xO1 = HudEditorScreen.MACRO.rightAligned ? HudEditorScreen.MACRO.getW() - client.textRenderer.getWidth(l1) : 0;
//                    int xO2 = HudEditorScreen.MACRO.rightAligned ? HudEditorScreen.MACRO.getW() - client.textRenderer.getWidth(l2) : 0;
//
//                    drawContext.drawText(client.textRenderer, l1, xO1, 0, 0xFFFFFFFF, true);
//                    drawContext.drawText(client.textRenderer, l2, 12, xO2, 0xFFFFFFFF, true);
//                });
//            }

            if (SaveGuiManager.savedScreen != null) {
                drawScaled(drawContext, client, HudEditorScreen.SAVED_GUI, () -> {
                    String line1 = "§dSaved GUI";
                    String line2 = "§8> " + (SaveGuiManager.deadGui ? "§c" : "§3") + SaveGuiManager.guiName
                            + (SaveGuiManager.deadGui ? "§c (clientside only)" : "");

                    int xOff1 = HudEditorScreen.SAVED_GUI.rightAligned ? HudEditorScreen.SAVED_GUI.getW() - client.textRenderer.getWidth(line1) : 0;
                    int xOff2 = HudEditorScreen.SAVED_GUI.rightAligned ? HudEditorScreen.SAVED_GUI.getW() - client.textRenderer.getWidth(line2) : 0;

                    drawContext.drawText(client.textRenderer, line1, xOff1, 0, 0xFFFFFFFF, true);
                    drawContext.drawText(client.textRenderer, line2, xOff2, 12, 0xFFFFFFFF, true);
                });
            }

            if (MainClient.MODULE_MANAGER.isEnabled(TpsCounterModule.class)) {
                if (lastPacketTime == -1) return;
                long timeSinceUpdate = System.currentTimeMillis() - lastPacketTime;
                double seconds = timeSinceUpdate / 1000.0;

                String text = (seconds > 10.0)
                        ? String.format("§dServer TPS: §l§cFROZEN §r§7(§f%.1fs§7)", seconds)
                        : String.format("§dServer TPS: %s%.1f", getTpsColorCode(tps), tps);

                drawScaled(drawContext, client, HudEditorScreen.TPS, () -> {
                    int xOff = HudEditorScreen.TPS.rightAligned ? HudEditorScreen.TPS.getW() - client.textRenderer.getWidth(text) : 0;
                    int color = (seconds > 10.0) ? 0xFFAA0000 : 0xFFFFFFFF;
                    drawContext.drawText(client.textRenderer, text, xOff, 0, color, true);
                });
            }

            HudModule hudModule = MainClient.MODULE_MANAGER.getModule(HudModule.class);
            if (hudModule != null && hudModule.isEnabled()) {
                drawScaled(drawContext, client, HudEditorScreen.HUD_LIST, () -> {
                    int i = 0;
                    for (Module module : MainClient.MODULE_MANAGER.getModules()) {
                        //yo if you see this fuck you litten this is the correct way to do it bro trust me
                        if (!hudModule.renderSetting.getValue() && module.getCategory().equals(Category.render)) continue;
                        if (!hudModule.miscSetting.getValue() && module.getCategory().equals(Category.misc)) continue;
                        if (!hudModule.glitchaSetting.getValue() && module.getCategory().equals(Category.glitcha)) continue;
                        //   if (!hudModule.exploitSetting.getValue() && module.getCategory().equals(Category.exploit)) continue;

                        if (module.isEnabled()) {
                            String name = "§d" + module.getName();
                            int xOff = HudEditorScreen.HUD_LIST.rightAligned ? HudEditorScreen.HUD_LIST.getW() - client.textRenderer.getWidth(name) : 0;
                            drawContext.drawText(client.textRenderer, name, xOff, i * 12, 0xFFFFFFFF, true);
                            i++;
                        }
                    }
                });
            }
        });
    }
}