package wtf.dupers.dupersunited.features.screens.hud;

import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.features.SaveGuiManager;
import wtf.dupers.dupersunited.features.TPSDisplay;
import wtf.dupers.dupersunited.modules.glitcha.TpsCounterModule;
import wtf.dupers.dupersunited.modules.render.HudModule;
import wtf.dupers.dupersunited.modules.render.WatermarkModule;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class HudEditorScreen extends Screen {

    public static final HudElement WATERMARK = new HudElement("Watermark", 4, 4);
    //public static final HudElement MACRO = new HudElement("Macro", 4, 21);
    public static final HudElement SAVED_GUI = new HudElement("Saved GUI", 4, 69);
    public static final HudElement TPS = new HudElement("TPS Counter", 4, 93);
    public static final HudElement HUD_LIST = new HudElement("HUD", 4, 117);

    private static final List<HudElement> ELEMENTS = List.of(WATERMARK, SAVED_GUI, TPS, HUD_LIST);
    private static final int GRID = 4;
    private static final float SCALE_STEP = 0.25f;
    private static final float SCALE_MIN  = 0.5f;
    private static final float SCALE_MAX  = 4.0f;
    private static final int RESET_W = 80;
    private static final int RESET_H = 12;

    private HudElement dragging = null;
    private int offX, offY;

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
    }

    private int snap(int value) {
        return Math.round((float) value / GRID) * GRID;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x80000000);

        for (HudElement el : ELEMENTS) {
            int sx = el.getScreenX(width);
            int w = el.getW();
            int h = el.getH();
            boolean hovered = el.isHovered(mouseX, mouseY, width);
            int border = hovered ? 0xFFDA70D6 : 0xFF888888;

            ctx.fill(sx, el.y, sx + w, el.y + h, 0x55000000);
            ctx.fill(sx, el.y, sx + w, el.y + 1, border);
            ctx.fill(sx, el.y + h - 1, sx + w, el.y + h, border);
            ctx.fill(sx, el.y, sx + 1, el.y + h, border);
            ctx.fill(sx + w - 1, el.y, sx + w, el.y + h, border);

            var matrices = ctx.getMatrices();
            matrices.pushMatrix();
            matrices.translate((float) sx + 2.0f, (float) el.y + 1.0f);
            matrices.scale(el.scale, el.scale);
            ctx.drawText(textRenderer, getPreview(el), 0, 0, 0xFFFFFFFF, true);
            matrices.popMatrix();

            if (hovered) {
                String alignIcon = el.rightAligned ? "◀" : "▶";
                String scaleLabel = String.format("%.1fx %s", el.scale, alignIcon);
                int labelX = sx + w - textRenderer.getWidth(scaleLabel) - 2;
                ctx.drawText(textRenderer, scaleLabel, labelX, el.y + 1, 0xFFCBA6F7, true);
            }
        }

        int btnX = width / 2 - RESET_W / 2;
        int btnY = height - 28;
        boolean btnHovered = mouseX >= btnX && mouseX <= btnX + RESET_W && mouseY >= btnY && mouseY <= btnY + RESET_H;
        ctx.fill(btnX, btnY, btnX + RESET_W, btnY + RESET_H, btnHovered ? 0xFFAA0000 : 0xFF880000);
        ctx.drawCenteredTextWithShadow(textRenderer, "§cReset HUD", btnX + RESET_W / 2, btnY + 2, 0xFFFFFFFF);

        ctx.drawCenteredTextWithShadow(textRenderer,
                "§7Drag to move  |  §dScroll §7to resize  |  §cESC §7to close",
                width / 2, height - 14, 0xFFFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudElement el : ELEMENTS) {
            if (el.isHovered(mouseX, mouseY, width)) {
                el.scale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, el.scale + (float) verticalAmount * SCALE_STEP));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // IGNORE THE HORRIBLE CODE IT DOES THE JOB FUCK YOU VINZY
    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (click.button() == 0) {
            int btnX = width / 2 - RESET_W / 2;
            int btnY = height - 28;
            if (click.x() >= btnX && click.x() <= btnX + RESET_W && click.y() >= btnY && click.y() <= btnY + RESET_H) {
                resetAll();
                return true;
            }

            for (HudElement el : ELEMENTS) {
                if (el.isHovered(click.x(), click.y(), width)) {
                    dragging = el;
                    int screenX = el.getScreenX(width);
                    el.rightAligned = false;
                    el.x = screenX;
                    offX = (int) click.x() - screenX;
                    offY = (int) click.y() - el.y;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragging != null) {
            int rawX = (int) click.x() - offX;
            int rawY = (int) click.y() - offY;

            dragging.x = snap(Math.max(0, Math.min(rawX, width - dragging.getW())));
            dragging.y = snap(Math.max(0, Math.min(rawY, height - dragging.getH())));
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            int centerX = dragging.x + dragging.getW() / 2;
            if (centerX > width / 2) {
                dragging.rightAligned = true;
                dragging.x = width - (dragging.x + dragging.getW());
            } else {
                dragging.rightAligned = false;
            }
            dragging = null;
        }
        return super.mouseReleased(click);
    }

    private void resetAll() {
        for (HudElement el : ELEMENTS) {
            el.scale = 1.0f;
            el.rightAligned = false;
        }
        WATERMARK.x = 4; WATERMARK.y = 4;
        //MACRO.x = 4; MACRO.y = 21;
        SAVED_GUI.x = 4; SAVED_GUI.y = 69;
        TPS.x = 4; TPS.y = 93;
        HUD_LIST.x = 4; HUD_LIST.y = 117;
    }

    private String getPreview(HudElement el) {
        switch (el.id) {
            case "Watermark": {
                WatermarkModule wm = MainClient.MODULE_MANAGER.getModule(WatermarkModule.class);
                return (wm != null && wm.isEnabled()) ? wm.watermarkText.getValue().replace("&", "§") : "§7Watermark §8(disabled)";
            }
//            case "Macro": {
//                return MacroManager.isRunning() ? "§dActive Macro: §5" + MacroManager.getRunningName() : "§dActive GUI Macro";
//            }
            case "Saved GUI": {
                return (SaveGuiManager.savedScreen != null) ? "§dSaved: " + (SaveGuiManager.deadGui ? "§c" : "§3") + SaveGuiManager.guiName : "§dSaved GUI";
            }
            case "TPS Counter": {
                if (!MainClient.MODULE_MANAGER.isEnabled(TpsCounterModule.class)) return "§7TPS Counter §8(disabled)";
                if (TPSDisplay.lastPacketTime == -1) return "§dServer TPS: §7--";
                return String.format("§dServer TPS: %s%.1f", TPSDisplay.getTpsColorCode(TPSDisplay.tps), TPSDisplay.tps);
            }
            case "HUD": {
                return !MainClient.MODULE_MANAGER.isEnabled(HudModule.class) ? "§7Module List §8(disabled)" : "§dModule List";
            }
            default: return el.id;
        }
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}