package wtf.dupers.dupersunited.features.screens;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.features.screens.hud.HudEditorScreen;
import wtf.dupers.dupersunited.features.screens.macroscreen.ChatMacroScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.KeybindScreen;
import wtf.dupers.dupersunited.api.keybind.Keybind;
import wtf.dupers.dupersunited.keybinds.KeybindManager;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.*;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static wtf.dupers.dupersunited.features.PrideTheme.*;
import static wtf.dupers.dupersunited.utils.ColorUtil.*;

public class ClickGui extends Screen {

    private static final int PW = 130;
    private static final int HEADER_H = 16;
    private static final int MOD_H = 13;
    private static final int SET_H = 13;
    private static final int SLD_H = 4;
    private static final int PAD = 6;

    private static final int C_BG = BG;
    private static final int C_BORDER = DEEP_INDIGO;
    private static final int C_HDR_BG = MANTLE;
    private static final int C_HDR_TXT = PALE_NAVY;
    private static final int C_HDR_BTN = HDR_BTN;
    private static final int C_ON = GREEN;
    private static final int C_OFF = OFF;
    private static final int C_HOVER = HOVER;
    private static final int C_ACCENT = GREEN;
    private static final int C_SET_BG = SET_BG;
    private static final int C_SET_LINE = SET_LINE;
    private static final int C_LBL = FADED_NAVY;
    private static final int C_GREEN = GREEN;
    private static final int C_RED = RED;
    private static final int C_BLUE = BLUE;
    private static final int C_ORANGE = PEACH;
    private static final int C_SLD_BG = DEEP_INDIGO;
    private static final int C_SLD_FG = GREEN;
    private static final int C_STR_FOCUS = FIELD_FOCUSED;
    private static final int C_CURSOR = GREEN;
    private static final int C_SELECTION = SELECTION;

    private final List<Panel> panels = new ArrayList<>();
    private HudPanel hudPanel;
    private final Screen parent;

    // <user customization>
    public static final Map<String, String> customCategories = new LinkedHashMap<>();
    public static final Map<String, Vector2i> categoryPositions = new HashMap<>();
    public static final Map<String, Set<String>> categoryExpandedModules = new HashMap<>();
    public static final Object2BooleanMap<String> categoryCollapsed = new Object2BooleanOpenHashMap<>();
    public static @Nullable Vector2i hudPanelPosition = null;
    // </user customization>

    private Panel focusedPanel = null;
    private String focusedMod = null;
    private String focusedSet = null;
    private int cursorPos = 0;
    private int selectionAnchor = -1;

    private String hoveredDescription = null;
    private String rebindingModule = null;
    private Keybind rebindingKeybind = null;
    private String searchQuery = "";
    private boolean searchFocused = false;

    private boolean hasSelection() {
        return selectionAnchor != -1 && selectionAnchor != cursorPos;
    }

    private void clearSelection() {
        selectionAnchor = -1;
    }

    private boolean isCtrlDown() {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private int selStart() {
        return Math.min(cursorPos, selectionAnchor);
    }

    private int selEnd() {
        return Math.max(cursorPos, selectionAnchor);
    }

    private void deleteSelection(StringSetting ss) {
        String cur = ss.getValue();
        int s = selStart(), e = selEnd();
        ss.setValue(cur.substring(0, s) + cur.substring(e));
        cursorPos = s;
        clearSelection();
    }

    private void deleteSearchSelection() {
        int s = selStart(), e = selEnd();
        searchQuery = searchQuery.substring(0, s) + searchQuery.substring(e);
        cursorPos = s;
        clearSelection();
    }

    private void clearFocus() {
        focusedPanel = null;
        focusedMod = null;
        focusedSet = null;
        searchFocused = false;
        cursorPos = 0;
        selectionAnchor = -1;
        rebindingModule = null;
        rebindingKeybind = null;
    }

    private void setFocus(Panel panel, String modName, String setName) {
        focusedPanel = panel;
        focusedMod = modName;
        focusedSet = setName;
        searchFocused = false;
    }

    private boolean isFocused(Panel panel, String modName, String setName) {
        return panel == focusedPanel && modName.equals(focusedMod) && setName.equals(focusedSet);
    }

    private String getKeyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        if (key >= 0 && key <= 7) return "MB" + (key + 1);
        String name = GLFW.glfwGetKeyName(key, 0);
        return name != null ? name.toUpperCase() : "K" + key;
    }

    private List<String> getAllCategories() {
        List<String> result = new ArrayList<>();
        for (Module m : MainClient.MODULE_MANAGER.modules()) {
            String cat = m.getCategory();
            if (!result.contains(cat)) result.add(cat);
        }
        return result;
    }

    private class HudPanel {
        int x, y;
        boolean dragging;
        int dox, doy;

        HudPanel(int x, int y) {
            if (hudPanelPosition == null) hudPanelPosition = new Vector2i(x, y);
            this.x = hudPanelPosition.x();
            this.y = hudPanelPosition.y();
        }

        void draw(DrawContext ctx, int mx, int my) {
            int ph = HEADER_H + MOD_H * 3;
            for (int i = 4; i >= 1; i--) {
                int a = (int) (255 * 0.055f * i);
                ctx.fill(x - i, y - i, x + PW + i, y + ph + i, a << 24);
            }
            ctx.fill(x, y, x + PW, y + ph, C_BG);
            ctx.fill(x, y, x + PW, y + 1, C_BORDER);
            ctx.fill(x, y + ph - 1, x + PW, y + ph, C_BORDER);
            ctx.fill(x, y, x + 1, y + ph, C_BORDER);
            ctx.fill(x + PW - 1, y, x + PW, y + ph, C_BORDER);
            ctx.fill(x, y, x + PW, y + HEADER_H, C_HDR_BG);
            ctx.fill(x, y + HEADER_H - 1, x + PW, y + HEADER_H, C_BORDER);
            if (PRIDE) ctx.drawTextWithShadow(textRenderer, prideStyle("Configs"), x + PAD, y + (HEADER_H - 7) / 2, -1);
            else ctx.drawTextWithShadow(textRenderer, "Configs", x + PAD, y + (HEADER_H - 7) / 2, C_HDR_TXT);
            int ry = y + HEADER_H;

            boolean hovHud = mx >= x && mx < x + PW && my >= ry && my < ry + MOD_H;
            if (hovHud) ctx.fill(x, ry, x + PW, ry + MOD_H, C_HOVER);
            ctx.fill(x, ry, x + 2, ry + MOD_H, PRIDE ? C_PRIDE_1 : C_BLUE);
            if (PRIDE) ctx.drawText(textRenderer, transStyle("Edit HUD"), x + PAD + 2, ry + 3, -1, false);
            else ctx.drawText(textRenderer, "Edit HUD", x + PAD + 2, ry + 3, C_BLUE, false);

            ry += MOD_H;
            boolean hovKb = mx >= x && mx < x + PW && my >= ry && my < ry + MOD_H;
            if (hovKb) ctx.fill(x, ry, x + PW, ry + MOD_H, C_HOVER);
            ctx.fill(x, ry, x + 2, ry + MOD_H, PRIDE ? C_PRIDE_1 : C_BLUE);
            if (PRIDE) ctx.drawText(textRenderer, transStyle("Keybinds"), x + PAD + 2, ry + 3, -1, false);
            else ctx.drawText(textRenderer, "Keybinds", x + PAD + 2, ry + 3, C_BLUE, false);

            ry += MOD_H;
            boolean hovCm = mx >= x && mx < x + PW && my >= ry && my < ry + MOD_H;
            if (hovCm) ctx.fill(x, ry, x + PW, ry + MOD_H, C_HOVER);
            ctx.fill(x, ry, x + 2, ry + MOD_H, PRIDE ? C_PRIDE_1 : C_BLUE);
            if (PRIDE) ctx.drawText(textRenderer, transStyle("ChatMacros"), x + PAD + 2, ry + 3, -1, false);
            else ctx.drawText(textRenderer, "ChatMacros", x + PAD + 2, ry + 3, C_BLUE, false);

            Keybind kb = KeybindManager.getRegisteredKeybinds().get("keybinds");
            if (kb != null) {
                String kbStr = (kb == rebindingKeybind) ? "..." : "[" + getKeyName(kb.getKeyCode()) + "]";
                int kbW = textRenderer.getWidth(kbStr);
                ctx.drawText(textRenderer, kbStr, x + PW - PAD - kbW, ry + 3, C_LBL, false);
            }
        }

        boolean mouseClicked(int mx, int my, int btn) {
            if (mx < x || mx >= x + PW) return false;
            if (my >= y && my < y + HEADER_H && btn == 0) {
                dragging = true;
                dox = mx - x;
                doy = my - y;
                return true;
            }
            int ry = y + HEADER_H;
            if (my >= ry && my < ry + MOD_H && btn == 0) {
                MinecraftClient.getInstance().setScreen(new HudEditorScreen());
                return true;
            }
            ry += MOD_H;
            if (my >= ry && my < ry + MOD_H) {
                if (btn == 0) {
                    MinecraftClient.getInstance().setScreen(new KeybindScreen(ClickGui.this));
                    return true;
                }
                if (btn == 2 || btn == 1) {
                    rebindingKeybind = KeybindManager.getRegisteredKeybinds().get("keybinds");
                    return true;
                }
            }
            ry += MOD_H;
            if (my >= ry && my < ry + MOD_H && btn == 0) {
                MinecraftClient.getInstance().setScreen(new ChatMacroScreen(ClickGui.this));
                return true;
            }
            return false;
        }

        void mouseDragged(int mx, int my) {
            if (dragging) {
                x = mx - dox;
                y = my - doy;
                hudPanelPosition.set(x, y);
            }
        }

        void mouseReleased() {
            dragging = false;
        }
    }

    private class Panel {
        final String category;
        final List<Module> modules;
        int x, y;
        boolean dragging;
        int dox, doy;
        boolean collapsed;
        final Set<String> expanded;
        String sliderMod, sliderSet;
        final List<Setting<?>> settings = new ObjectArrayList<>();

        Panel(String category, List<Module> modules, int x, int y) {
            this.category = category;
            this.modules = modules;

            Vector2i position = categoryPositions.computeIfAbsent(category, k -> new Vector2i(x, y));
            this.x = position.x();
            this.y = position.y();
            this.expanded = categoryExpandedModules.computeIfAbsent(category, k -> new ObjectOpenHashSet<>());
            this.collapsed = categoryCollapsed.computeIfAbsent(category, k -> false);
        }

        private List<Module> getFilteredModules() {
            if (searchQuery.isEmpty()) return modules;
            List<Module> filtered = new ArrayList<>();
            for (Module m : modules) {
                if (m.getName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))) {
                    filtered.add(m);
                }
            }
            return filtered;
        }

        private void populateSettings(Module module) {
            settings.clear();
            for (Setting<?> s : module.getSettings()) {
                if (s.visible.getAsBoolean()) settings.add(s);
            }
        }

        int height() {
            if (collapsed) return HEADER_H;
            int h = HEADER_H;
            List<Module> filtered = getFilteredModules();
            if (filtered.isEmpty() && !searchQuery.isEmpty()) return HEADER_H + MOD_H;
            for (Module m : filtered) {
                h += MOD_H;
                if (expanded.contains(m.getName())) {
                    populateSettings(m);
                    if (!settings.isEmpty()) h += settBlockH(settings);
                }
            }
            return h;
        }

        int settBlockH(List<Setting<?>> ss) {
            int h = 4;
            for (Setting<?> s : ss) {
                if (s instanceof FloatSetting || s instanceof IntSetting) h += SET_H + SLD_H + 3;
                else h += SET_H;
            }
            return h;
        }

        void draw(DrawContext ctx, int mx, int my) {
            int pw = PW, ph = height();
            for (int i = 4; i >= 1; i--) {
                int a = (int) (255 * 0.055f * i);
                ctx.fill(x - i, y - i, x + pw + i, y + ph + i, a << 24);
            }
            ctx.fill(x, y, x + pw, y + ph, C_BG);
            ctx.fill(x, y, x + pw, y + 1, C_BORDER);
            ctx.fill(x, y + ph - 1, x + pw, y + ph, C_BORDER);
            ctx.fill(x, y, x + 1, y + ph, C_BORDER);
            ctx.fill(x + pw - 1, y, x + pw, y + ph, C_BORDER);
            ctx.fill(x, y, x + pw, y + HEADER_H, C_HDR_BG);
            ctx.fill(x, y + HEADER_H - 1, x + pw, y + HEADER_H, C_BORDER);
            if (PRIDE) ctx.drawTextWithShadow(textRenderer, prideStyle(category), x + PAD, y + (HEADER_H - 7) / 2, -1);
            else ctx.drawTextWithShadow(textRenderer, category, x + PAD, y + (HEADER_H - 7) / 2, C_HDR_TXT);
            ctx.drawText(textRenderer, collapsed ? "+" : "—", x + pw - 10, y + (HEADER_H - 7) / 2, C_HDR_BTN, false);

            if (collapsed) return;

            int ry = y + HEADER_H;
            List<Module> filtered = getFilteredModules();
            if (filtered.isEmpty() && !searchQuery.isEmpty()) {
                ctx.drawText(textRenderer, "No results", x + PAD, ry + 3, C_OFF, false);
                return;
            }

            for (Module mod : filtered) {
                boolean hov = mx >= x && mx < x + pw && my >= ry && my < ry + MOD_H;
                boolean on = mod.isEnabled();
                boolean exp = expanded.contains(mod.getName());
                if (hov) {
                    ctx.fill(x, ry, x + pw, ry + MOD_H, C_HOVER);
                    hoveredDescription = mod.getDescription();
                }
                if (on) ctx.fill(x, ry, x + 2, ry + MOD_H, PRIDE ? C_PRIDE_1 : C_ACCENT);
                int tx = x + PAD + (on ? 2 : 0);
                if (on) {
                    if (PRIDE) ctx.drawTextWithShadow(textRenderer, transStyle(mod.getName()), tx, ry + 3, -1);
                    else ctx.drawTextWithShadow(textRenderer, mod.getName(), tx, ry + 3, C_ON);
                }
                else ctx.drawText(textRenderer, mod.getName(), tx, ry + 3, C_OFF, false);

                populateSettings(mod);
                if (!settings.isEmpty())
                    ctx.drawText(textRenderer, exp ? "▾" : "▸", x + pw - 9, ry + 3, C_LBL, false);
                ry += MOD_H;

                if (exp) {
                    List<Setting<?>> ss = settings;
                    if (!ss.isEmpty()) {
                        int bh = settBlockH(ss);
                        ctx.fill(x, ry, x + pw, ry + bh, C_SET_BG);
                        ctx.fill(x, ry, x + 1, ry + bh, C_SET_LINE);
                        ctx.fill(x + pw - 1, ry, x + pw, ry + bh, C_SET_LINE);
                        ctx.fill(x, ry + bh - 1, x + pw, ry + bh, C_SET_LINE);

                        int sy = ry + 3;
                        int sx = x + PAD + 2;
                        int sw = pw - PAD * 2 - 2;

                        for (Setting<?> s : ss) {
                            if (s instanceof FloatSetting fs) {
                                float val = fs.getValue();
                                String lbl = s.getName() + ": ";
                                String vs = (val == (int) val) ? String.valueOf((int) val) : String.format("%.1f", val);
                                ctx.drawText(textRenderer, lbl, sx, sy, C_LBL, false);
                                ctx.drawText(textRenderer, vs, sx + textRenderer.getWidth(lbl), sy, C_BLUE, false);
                                int barY = sy + SET_H - 2;
                                float pct = Math.max(0, Math.min(1, (val - fs.getMin()) / (fs.getMax() - fs.getMin())));
                                int fw = (int) (sw * pct);
                                ctx.fill(sx, barY, sx + sw, barY + SLD_H, C_SLD_BG);
                                if (fw > 0) ctx.fill(sx, barY, sx + fw, barY + SLD_H, C_SLD_FG);
                                sy += SET_H + SLD_H + 3;

                            } else if (s instanceof IntSetting is) {
                                int val = is.getValue();
                                String lbl = s.getName() + ": ";
                                ctx.drawText(textRenderer, lbl, sx, sy, C_LBL, false);
                                ctx.drawText(textRenderer, String.valueOf(val), sx + textRenderer.getWidth(lbl), sy, C_BLUE, false);
                                int barY = sy + SET_H - 2;
                                float pct = Math.max(0, Math.min(1, (float) (val - is.getMin()) / (is.getMax() - is.getMin())));
                                int fw = (int) (sw * pct);
                                ctx.fill(sx, barY, sx + sw, barY + SLD_H, C_SLD_BG);
                                if (fw > 0) ctx.fill(sx, barY, sx + fw, barY + SLD_H, C_SLD_FG);
                                sy += SET_H + SLD_H + 3;

                            } else if (s instanceof BooleanSetting bs) {
                                boolean v = bs.getValue();
                                String lbl = s.getName() + ": ";
                                ctx.drawText(textRenderer, lbl, sx, sy, C_LBL, false);
                                ctx.drawText(textRenderer, v ? "true" : "false", sx + textRenderer.getWidth(lbl), sy, v ? C_GREEN : C_RED, false);
                                sy += SET_H;

                            } else if (s instanceof ModeSetting ms) {
                                String lbl = s.getName() + ": ";
                                ctx.drawText(textRenderer, lbl, sx, sy, C_LBL, false);
                                ctx.drawText(textRenderer, ms.getValue(), sx + textRenderer.getWidth(lbl), sy, C_ORANGE, false);
                                sy += SET_H;

                            } else if (s instanceof ButtonSetting bs) {
                                boolean hov2 = mx >= x && mx < x + pw && my >= sy && my < sy + SET_H;
                                if (hov2) ctx.fill(x, sy, x + pw, sy + SET_H, C_HOVER);
                                ctx.fill(x, sy, x + 2, sy + SET_H, C_BLUE);
                                ctx.drawText(textRenderer, bs.getName(), sx + 2, sy + 3, C_BLUE, false);
                                sy += SET_H;

                            } else if (s instanceof BindSetting bs) {
                                boolean isRebinding = mod.getName().equals(rebindingModule) && s.getName().equals(focusedSet);
                                String lbl = s.getName() + ": ";
                                String val = isRebinding ? "..." : "[" + bs.getKeyName() + "]";
                                ctx.drawText(textRenderer, lbl, sx, sy, C_LBL, false);
                                ctx.drawText(textRenderer, val, sx + textRenderer.getWidth(lbl), sy, C_ORANGE, false);
                                sy += SET_H;

                            } else if (s instanceof StringSetting ss2) {
                                boolean focused = isFocused(this, mod.getName(), s.getName());
                                String lbl = s.getName() + ": ";
                                String val = ss2.getValue();
                                String renderedVal = val.replace('&', '§');
                                int valueX = sx + textRenderer.getWidth(lbl);
                                int textEndX = valueX + textRenderer.getWidth(val);
                                if (focused) {
                                    int bgRight = textEndX + (val.isEmpty() ? 4 : 2);
                                    ctx.fill(sx - 1, sy - 1, bgRight + 1, sy + SET_H - 1, C_STR_FOCUS);
                                    if (hasSelection()) {
                                        int s1 = valueX + textRenderer.getWidth(val.substring(0, selStart()));
                                        int s2 = valueX + textRenderer.getWidth(val.substring(0, selEnd()));
                                        ctx.fill(s1, sy - 1, s2, sy + SET_H - 1, C_SELECTION);
                                    }
                                }
                                ctx.drawText(textRenderer, lbl, sx, sy, C_LBL, false);
                                if (focused) {
                                    ctx.drawText(textRenderer, renderedVal, valueX, sy, C_HDR_TXT, false);
                                    long now = System.currentTimeMillis();
                                    if (!hasSelection() && (now / 500) % 2 == 0) {
                                        int clampedCursor = Math.min(cursorPos, val.length());
                                        int cursorDrawX = valueX + textRenderer.getWidth(val.substring(0, clampedCursor));
                                        ctx.drawText(textRenderer, "|", cursorDrawX, sy, C_CURSOR, false);
                                    }
                                } else {
                                    int clipRight = sx + sw;
                                    ctx.enableScissor(valueX, sy - 1, clipRight, sy + SET_H);
                                    ctx.drawText(textRenderer, renderedVal, valueX, sy, C_HDR_TXT, false);
                                    ctx.disableScissor();
                                }
                                sy += SET_H;
                            }
                        }
                        ry += bh;
                    }
                }
            }
        }

        boolean mouseClicked(int mx, int my, int btn) {
            if (mx < x || mx >= x + PW) return false;

            if (my >= y && my < y + HEADER_H && btn == 0) {
                if (mx >= x + PW - 12) {
                    collapsed = !collapsed;
                    categoryCollapsed.put(category, collapsed);
                    return true;
                }
                dragging = true;
                dox = mx - x;
                doy = my - y;
                return true;
            }

            if (collapsed) return false;

            int ry = y + HEADER_H;
            List<Module> filtered = getFilteredModules();
            for (Module mod : filtered) {
                populateSettings(mod);
                if (my >= ry && my < ry + MOD_H) {
                    if (btn == 0) {
                        mod.toggle();
                        return true;
                    }
                    if (btn == 1) {
                        if (!settings.isEmpty()) {
                            toggleExpand(mod.getName());
                            return true;
                        }
                    }
                    return false;
                }
                ry += MOD_H;

                if (expanded.contains(mod.getName())) {
                    List<Setting<?>> ss = settings;
                    if (!ss.isEmpty()) {
                        int bh = settBlockH(ss);
                        if (my >= ry && my < ry + bh) {
                            int sy = ry + 3;
                            int sx = x + PAD + 2;
                            int sw = PW - PAD * 2 - 2;
                            for (Setting<?> s : ss) {
                                if (s instanceof FloatSetting fs) {
                                    int barY = sy + SET_H - 2;
                                    if (my >= sy && my <= barY + SLD_H + 2 && btn == 0) {
                                        float pct = (float) (mx - sx) / sw;
                                        fs.setValue(fs.getMin() + pct * (fs.getMax() - fs.getMin()));
                                        sliderMod = mod.getName();
                                        sliderSet = fs.getName();
                                        clearFocus();
                                        return true;
                                    }
                                    sy += SET_H + SLD_H + 3;
                                } else if (s instanceof IntSetting is) {
                                    int barY = sy + SET_H - 2;
                                    if (my >= sy && my <= barY + SLD_H + 2 && btn == 0) {
                                        float pct = (float) (mx - sx) / sw;
                                        is.setValue(Math.round(is.getMin() + pct * (is.getMax() - is.getMin())));
                                        sliderMod = mod.getName();
                                        sliderSet = is.getName();
                                        clearFocus();
                                        return true;
                                    }
                                    sy += SET_H + SLD_H + 3;
                                } else if (s instanceof BooleanSetting bs) {
                                    if (my >= sy && my < sy + SET_H && (btn == 0 || btn == 1)) {
                                        bs.toggle();
                                        clearFocus();
                                        return true;
                                    }
                                    sy += SET_H;
                                } else if (s instanceof ModeSetting ms) {
                                    if (my >= sy && my < sy + SET_H && (btn == 0 || btn == 1)) {
                                        List<String> opts = ms.getOptions();
                                        int idx = opts.indexOf(ms.getValue());
                                        if (btn == 0) idx = (idx + 1) % opts.size();
                                        else idx = (idx - 1 + opts.size()) % opts.size();
                                        ms.setValue(opts.get(idx));
                                        clearFocus();
                                        return true;
                                    }
                                    sy += SET_H;
                                } else if (s instanceof ButtonSetting bs) {
                                    if (my >= sy && my < sy + SET_H && (btn == 0 || btn == 1)) {
                                        bs.press();
                                        clearFocus();
                                        return true;
                                    }
                                    sy += SET_H;
                                } else if (s instanceof BindSetting) {
                                    if (my >= sy && my < sy + SET_H && btn == 0) {
                                        setFocus(this, mod.getName(), s.getName());
                                        rebindingModule = mod.getName();
                                        return true;
                                    }
                                    sy += SET_H;
                                } else if (s instanceof StringSetting ss2) {
                                    if (my >= sy && my < sy + SET_H && btn == 0) {
                                        setFocus(this, mod.getName(), s.getName());
                                        clearSelection();
                                        String val = ss2.getValue();
                                        int valueX = sx + textRenderer.getWidth(s.getName() + ": ");
                                        int bestPos = val.length(), bestDist = Integer.MAX_VALUE;
                                        for (int i = 0; i <= val.length(); i++) {
                                            int cx = valueX + textRenderer.getWidth(val.substring(0, i));
                                            int dist = Math.abs(mx - cx);
                                            if (dist < bestDist) {
                                                bestDist = dist;
                                                bestPos = i;
                                            }
                                        }
                                        cursorPos = bestPos;
                                        return true;
                                    }
                                    sy += SET_H;
                                }
                            }
                            clearFocus();
                            return true;
                        }
                        ry += bh;
                    }
                }
            }
            return false;
        }

        void mouseDragged(int mx, int my) {
            if (dragging) {
                x = mx - dox;
                y = my - doy;
                categoryPositions.get(category).set(x, y);
            }
            if (sliderMod != null) {
                Module mod = MainClient.MODULE_MANAGER.getModuleByName(sliderMod);
                if (mod != null) {
                    Setting<?> s = mod.getSettingByName(sliderSet);
                    int sx = x + PAD + 2, sw = PW - PAD * 2 - 2;
                    float pct = (float) (mx - sx) / sw;
                    if (s instanceof FloatSetting fs) fs.setValue(fs.getMin() + pct * (fs.getMax() - fs.getMin()));
                    else if (s instanceof IntSetting is)
                        is.setValue(Math.round(is.getMin() + pct * (is.getMax() - is.getMin())));
                }
            }
        }

        void mouseReleased() {
            dragging = false;
            sliderMod = null;
            sliderSet = null;
        }

        void toggleExpand(String name) {
            if (!expanded.add(name)) expanded.remove(name);
        }
    }

    public ClickGui(Screen parent) {
        super(Text.literal("ClickGUI"));
        if (this.getClass().isInstance(parent)) this.parent = null;
        else this.parent = parent;
    }

    @Override
    protected void init() {
        panels.clear();
        Map<String, List<Module>> cats = new LinkedHashMap<>();
        for (Module m : MainClient.MODULE_MANAGER.modules())
            cats.computeIfAbsent(customCategories.getOrDefault(m.getName(), m.getCategory()), k -> new ArrayList<>()).add(m);

        int maxWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int col = 0;
        Int2IntMap heights = new Int2IntOpenHashMap();

        for (Map.Entry<String, List<Module>> e : cats.entrySet()) {
            int x = 6 + col * (PW + 4);
            if (x + PW > maxWidth) {
                col = 0;
                x = 6;
            }
            int y = 4 + heights.getOrDefault(col, 2);

            Panel panel = new Panel(e.getKey(), e.getValue(), x, y);
            panels.add(panel);

            heights.put(col, panel.y + panel.height());
            col++;
        }

        if (hudPanel == null) {
            int x = 6 + col * (PW + 4);
            if (x + PW > maxWidth) {
                col = 0;
                x = 6;
            }
            int y = 4 + heights.getOrDefault(col, 2);

            hudPanel = new HudPanel(x, y);
        }
    }

    private void drawSearchBar(DrawContext ctx, int mx, int my) {
        int w = PW + 20;
        int h = HEADER_H;
        int x = (this.width - w) / 2;
        int y = this.height - 30;

        ctx.fill(x, y, x + w, y + h, C_BG);
        ctx.fill(x, y, x + w, y + 1, C_BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, C_BORDER);
        ctx.fill(x, y, x + 1, y + h, C_BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, C_BORDER);

        if (searchFocused) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, C_STR_FOCUS);
            if (hasSelection()) {
                int s1 = x + PAD + textRenderer.getWidth(searchQuery.substring(0, selStart()));
                int s2 = x + PAD + textRenderer.getWidth(searchQuery.substring(0, selEnd()));
                ctx.fill(s1, y + 2, s2, y + h - 2, C_SELECTION);
            }
        }

        String display = searchQuery.isEmpty() && !searchFocused ? "Search Modules..." : searchQuery;
        int color = searchQuery.isEmpty() && !searchFocused ? C_OFF : C_HDR_TXT;
        ctx.drawText(textRenderer, display, x + PAD, y + 4, color, false);

        if (searchFocused) {
            long now = System.currentTimeMillis();
            if (!hasSelection() && (now / 500) % 2 == 0) {
                int cursorX = x + PAD + textRenderer.getWidth(searchQuery.substring(0, cursorPos));
                ctx.drawText(textRenderer, "|", cursorX, y + 4, C_CURSOR, false);
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x99000000);
        drawSearchBar(ctx, mouseX, mouseY);
        hoveredDescription = null;
        Panel top = null;
        for (Panel p : panels) if (p.dragging) top = p;
        for (Panel p : panels) if (p != top) p.draw(ctx, mouseX, mouseY);
        if (top != null) top.draw(ctx, mouseX, mouseY);
        hudPanel.draw(ctx, mouseX, mouseY);
        if (hoveredDescription != null && !hoveredDescription.isEmpty()) {
            int tw = textRenderer.getWidth(hoveredDescription);
            int tx = mouseX + 10, ty = mouseY + 10;
            ctx.fill(tx - 2, ty - 2, tx + tw + 2, ty + 12, MANTLE);
            ctx.fill(tx - 3, ty - 3, tx + tw + 3, ty - 2, DEEP_INDIGO);
            ctx.fill(tx - 3, ty + 12, tx + tw + 3, ty + 13, DEEP_INDIGO);
            ctx.fill(tx - 3, ty - 2, tx - 2, ty + 12, DEEP_INDIGO);
            ctx.fill(tx + tw + 2, ty - 2, tx + tw + 3, ty + 12, DEEP_INDIGO);
            ctx.drawText(textRenderer, hoveredDescription, tx, ty + 1, C_HDR_TXT, false);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        int btn = click.button();

        if (rebindingKeybind != null) {
            rebindingKeybind.setKeyCode(btn);
            rebindingKeybind = null;
            return true;
        }

        if (rebindingModule != null && focusedSet != null) {
            Module mod = MainClient.MODULE_MANAGER.getModuleByName(rebindingModule);
            if (mod != null) {
                Setting<?> s = mod.getSettingByName(focusedSet);
                if (s instanceof BindSetting bs) {
                    boolean hitAnyPanel = false;

                    if (mx >= hudPanel.x && mx <= hudPanel.x + PW && my >= hudPanel.y && my <= hudPanel.y + (HEADER_H + MOD_H * 2)) {
                        hitAnyPanel = true;
                    }

                    if (!hitAnyPanel) {
                        for (Panel p : panels) {
                            if (mx >= p.x && mx <= p.x + PW && my >= p.y && my <= p.y + p.height()) {
                                hitAnyPanel = true;
                                break;
                            }
                        }
                    }

                    if (!hitAnyPanel) {
                        bs.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
                    } else {
                        bs.setKeyCode(btn);
                    }

                    ConfigManager.save();
                    clearFocus();
                    return true;
                }
            }
            clearFocus();
            return true;
        }

        int sw = PW + 20, sh = HEADER_H;
        int sx = (this.width - sw) / 2;
        int sy = this.height - 30;
        if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + sh) {
            clearFocus();
            searchFocused = true;
            clearSelection();
            int bestPos = searchQuery.length(), bestDist = Integer.MAX_VALUE;
            int startX = sx + PAD;
            for (int i = 0; i <= searchQuery.length(); i++) {
                int cx = startX + textRenderer.getWidth(searchQuery.substring(0, i));
                int dist = Math.abs(mx - cx);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestPos = i;
                }
            }
            cursorPos = bestPos;
            return true;
        }

        if (hudPanel.mouseClicked(mx, my, btn)) return true;

        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mx, my, btn)) {
                panels.add(panels.remove(i));
                return true;
            }
        }

        clearFocus();
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        hudPanel.mouseDragged((int) click.x(), (int) click.y());
        panels.forEach(p -> p.mouseDragged((int) click.x(), (int) click.y()));
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        hudPanel.mouseReleased();
        panels.forEach(Panel::mouseReleased);
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int keyCode = input.getKeycode();

        if (rebindingKeybind != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                rebindingKeybind.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                rebindingKeybind.setKeyCode(keyCode);
            }
            rebindingKeybind = null;
            return true;
        }

        if (rebindingModule != null && focusedSet != null) {
            Module mod = MainClient.MODULE_MANAGER.getModuleByName(rebindingModule);
            if (mod != null) {
                Setting<?> s = mod.getSettingByName(focusedSet);
                if (s instanceof BindSetting bs) {
                    if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                        bs.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
                    } else {
                        bs.setKeyCode(keyCode);
                    }
                    ConfigManager.save();
                    clearFocus();
                    return true;
                }
            }
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                searchFocused = false;
                return true;
            }
            boolean ctrl = isCtrlDown();
            if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
                selectionAnchor = 0;
                cursorPos = searchQuery.length();
                return true;
            }
            if (ctrl && keyCode == GLFW.GLFW_KEY_C) {
                if (hasSelection()) MinecraftClient.getInstance().keyboard.setClipboard(searchQuery.substring(selStart(), selEnd()));
                return true;
            }
            if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
                String cb = MinecraftClient.getInstance().keyboard.getClipboard();
                if (cb != null && !cb.isEmpty()) {
                    if (hasSelection()) deleteSearchSelection();
                    searchQuery = searchQuery.substring(0, cursorPos) + cb + searchQuery.substring(cursorPos);
                    cursorPos += cb.length();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (hasSelection()) deleteSearchSelection();
                else if (cursorPos > 0) {
                    searchQuery = searchQuery.substring(0, cursorPos - 1) + searchQuery.substring(cursorPos);
                    cursorPos--;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (hasSelection()) deleteSearchSelection();
                else if (cursorPos < searchQuery.length()) searchQuery = searchQuery.substring(0, cursorPos) + searchQuery.substring(cursorPos + 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (hasSelection()) { cursorPos = selStart(); clearSelection(); }
                else if (cursorPos > 0) cursorPos--;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (hasSelection()) { cursorPos = selEnd(); clearSelection(); }
                else if (cursorPos < searchQuery.length()) cursorPos++;
                return true;
            }
            return true;
        }

        if (focusedMod != null && focusedSet != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                clearFocus();
                return true;
            }
            Module mod = MainClient.MODULE_MANAGER.getModuleByName(focusedMod);
            if (mod != null) {
                Setting<?> s = mod.getSettingByName(focusedSet);
                if (s instanceof StringSetting ss) {
                    String cur = ss.getValue();
                    cursorPos = Math.min(cursorPos, cur.length());
                    boolean ctrl = isCtrlDown();
                    if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
                        selectionAnchor = 0;
                        cursorPos = cur.length();
                        return true;
                    }
                    if (ctrl && keyCode == GLFW.GLFW_KEY_C) {
                        if (hasSelection())
                            MinecraftClient.getInstance().keyboard.setClipboard(ss.getValue().substring(selStart(), selEnd()));
                        return true;
                    }
                    if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
                        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
                        if (clipboard != null && !clipboard.isEmpty()) {
                            if (hasSelection()) deleteSelection(ss);
                            cur = ss.getValue();
                            cursorPos = Math.min(cursorPos, cur.length());
                            ss.setValue(cur.substring(0, cursorPos) + clipboard + cur.substring(cursorPos));
                            cursorPos = Math.min(cursorPos + clipboard.length(), ss.getValue().length());
                        }
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                        if (hasSelection()) deleteSelection(ss);
                        else if (cursorPos > 0) {
                            ss.setValue(cur.substring(0, cursorPos - 1) + cur.substring(cursorPos));
                            cursorPos--;
                        }
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_DELETE) {
                        if (hasSelection()) deleteSelection(ss);
                        else if (cursorPos < cur.length())
                            ss.setValue(cur.substring(0, cursorPos) + cur.substring(cursorPos + 1));
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_LEFT) {
                        if (hasSelection()) {
                            cursorPos = selStart();
                            clearSelection();
                        } else if (cursorPos > 0) cursorPos--;
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                        if (hasSelection()) {
                            cursorPos = selEnd();
                            clearSelection();
                        } else if (cursorPos < cur.length()) cursorPos++;
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_HOME) {
                        cursorPos = 0;
                        clearSelection();
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_END) {
                        cursorPos = cur.length();
                        clearSelection();
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_TAB) {
                        clearFocus();
                        return true;
                    }
                    return true;
                }
            }
            clearFocus();
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        if (searchFocused && input.isValidChar()) {
            if (hasSelection()) deleteSearchSelection();
            searchQuery = searchQuery.substring(0, cursorPos) + input.asString() + searchQuery.substring(cursorPos);
            cursorPos++;
            return true;
        }

        if (focusedMod != null && focusedSet != null) {
            Module mod = MainClient.MODULE_MANAGER.getModuleByName(focusedMod);
            if (mod != null) {
                Setting<?> s = mod.getSettingByName(focusedSet);
                if (s instanceof StringSetting ss) {
                    if (input.isValidChar()) {
                        if (hasSelection()) deleteSelection(ss);
                        String cur = ss.getValue();
                        cursorPos = Math.min(cursorPos, cur.length());
                        ss.setValue(cur.substring(0, cursorPos) + input.asString() + cur.substring(cursorPos));
                        cursorPos++;
                    }
                    return true;
                }
            }
            clearFocus();
        }
        return super.charTyped(input);
    }

    @Override
    public void close() {
        ConfigManager.save();
        assert this.client != null;
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}