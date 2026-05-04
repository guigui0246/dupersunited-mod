package com.vinzy.cataddons.features.screens;

import com.vinzy.cataddons.utils.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Predicate;

public class EntitySelectionScreen extends Screen {

    private static final int BG = ColorUtil.DEEP_SAPPHIRE;
    private static final int SURFACE = ColorUtil.DEEP_INDIGO;
    private static final int OVERLAY = ColorUtil.FADED_INDIGO;
    private static final int MAUVE = ColorUtil.MAUVE;
    private static final int TEXT_COL = ColorUtil.PALE_NAVY;
    private static final int SUBTEXT = ColorUtil.SUBTEXT;
    private static final int TEAL = ColorUtil.TEAL;

    private static final int PAD = 10;
    private static final int SEARCH_TOP_OFF = 20;
    private static final int SEARCH_H = 13;
    private static final int AFTER_SEARCH_GAP = 5;
    private static final int TOGGLE_H = 12;
    private static final int TOGGLE_INNER_GAP = 3;
    private static final int AFTER_TOGGLE_CLEAR_GAP = 4;
    private static final int HEADER_H = 74;
    private static final int HEADER_AFTER_LIST_GAP = 6;
    private static final int FOOTER_H = 16;
    private static final int LINE_H = 14;
    private static final int LIST_PANEL_MIN_W = 200;
    private static final int LIST_PANEL_MAX_W = 260;

    private static final String CLEAR_BTN_LABEL = "Clear";
    private static final int CLEAR_BTN_H = 14;

    private final Set<EntityType<?>> selection;
    private final Runnable saveAction;
    private final String headerTitle;

    private final List<EntityType<?>> allTypes = new ArrayList<>();
    private final List<EntityType<?>> shown = new ArrayList<>();
    private String searchQuery = "";
    private int listScroll = 0;

    private int cursorPos = 0;
    private long lastBlink = 0;
    private boolean cursorVisible = true;

    private boolean listDragActive;
    private boolean listDragAdd;
    private int listDragLastShownIndex = -1;

    public EntitySelectionScreen(Text screenTitle, String headerTitle, Set<EntityType<?>> selection, Runnable saveAction) {
        super(screenTitle);
        this.headerTitle = headerTitle;
        this.selection = selection;
        this.saveAction = saveAction;
        Registries.ENTITY_TYPE.forEach(allTypes::add);
        allTypes.sort(Comparator.comparing(t -> Registries.ENTITY_TYPE.getId(t).toString()));
        rebuildShown();
    }

    private static final Set<String> EXCLUDED_FROM_ALL_BULK_IDS = Set.of(
            "minecraft:player",
            "minecraft:text_display",
            "minecraft:block_display",
            "minecraft:item_display"
    );

    private static boolean isExcludedFromAllBulk(EntityType<?> t) {
        return EXCLUDED_FROM_ALL_BULK_IDS.contains(Registries.ENTITY_TYPE.getId(t).toString());
    }

    private static final Set<String> FORCE_PASSIVE_BULK_IDS = Set.of(
            "minecraft:copper_golem",
            "minecraft:snow_golem"
    );

    private static boolean isMonster(EntityType<?> t) {
        if (isExcludedFromAllBulk(t)) {
            return false;
        }
        String id = Registries.ENTITY_TYPE.getId(t).toString();
        if (FORCE_PASSIVE_BULK_IDS.contains(id)) {
            return false;
        }
        return t.getSpawnGroup() == SpawnGroup.MONSTER;
    }

    private static boolean isPassive(EntityType<?> t) {
        if (isExcludedFromAllBulk(t)) {
            return false;
        }
        String id = Registries.ENTITY_TYPE.getId(t).toString();
        if (FORCE_PASSIVE_BULK_IDS.contains(id)) {
            return true;
        }
        SpawnGroup g = t.getSpawnGroup();
        return g == SpawnGroup.CREATURE
                || g == SpawnGroup.AMBIENT
                || g == SpawnGroup.WATER_CREATURE
                || g == SpawnGroup.WATER_AMBIENT
                || g == SpawnGroup.UNDERGROUND_WATER_CREATURE
                || g == SpawnGroup.AXOLOTLS;
    }

    private static boolean isItemEntity(EntityType<?> t) {
        return t == EntityType.ITEM;
    }

    private static boolean isMisc(EntityType<?> t) {
        if (isExcludedFromAllBulk(t)) {
            return false;
        }
        String id = Registries.ENTITY_TYPE.getId(t).toString();
        if (FORCE_PASSIVE_BULK_IDS.contains(id)) {
            return false;
        }
        if (t == EntityType.ITEM) {
            return false;
        }
        if (t.getSpawnGroup() != SpawnGroup.MISC) {
            return false;
        }
        String path = Registries.ENTITY_TYPE.getId(t).getPath();
        return !path.endsWith("_golem");
    }

    private int toggleRowY(int panelY) {
        return panelY + SEARCH_TOP_OFF + SEARCH_H + AFTER_SEARCH_GAP;
    }

    private boolean allInCategorySelected(Predicate<EntityType<?>> pred) {
        boolean any = false;
        for (EntityType<?> t : allTypes) {
            if (!pred.test(t)) {
                continue;
            }
            any = true;
            if (!selection.contains(t)) {
                return false;
            }
        }
        return any;
    }

    private void toggleCategory(Predicate<EntityType<?>> pred) {
        List<EntityType<?>> types = new ArrayList<>();
        for (EntityType<?> t : allTypes) {
            if (pred.test(t)) {
                types.add(t);
            }
        }
        if (types.isEmpty()) {
            return;
        }
        boolean all = selection.containsAll(types);
        if (all) {
            types.forEach(selection::remove);
        } else {
            selection.addAll(types);
        }
        saveAction.run();
    }

    private void rebuildShown() {
        shown.clear();
        String q = searchQuery.toLowerCase(Locale.ROOT);
        for (EntityType<?> t : allTypes) {
            String id = Registries.ENTITY_TYPE.getId(t).toString();
            String name = Text.translatable(t.getTranslationKey()).getString().toLowerCase(Locale.ROOT);
            if (q.isEmpty() || name.contains(q) || id.contains(q)) {
                shown.add(t);
            }
        }
        listScroll = 0;
    }

    private int panelW() {
        return Math.min(LIST_PANEL_MAX_W, Math.max(LIST_PANEL_MIN_W, width - 100));
    }

    private int panelX() {
        return (width - panelW()) / 2;
    }

    private int listAreaTop(int panelY) {
        return panelY + HEADER_H + HEADER_AFTER_LIST_GAP;
    }

    private int listHitShownIndex(int mx, int my, int px, int pw, int py, int ph) {
        int visLines = visibleListLines(ph);
        int listTop = listAreaTop(py);
        int rx1 = px + PAD;
        int rx2 = px + pw - PAD;
        if (mx < rx1 || mx >= rx2) {
            return -1;
        }
        int endIdx = Math.min(listScroll + visLines, shown.size());
        for (int i = listScroll; i < endIdx; i++) {
            int row = i - listScroll;
            int ry = listTop + row * LINE_H;
            if (my >= ry && my < ry + LINE_H) {
                return i;
            }
        }
        return -1;
    }

    private void applyListDragRange(int fromShown, int toShown) {
        if (shown.isEmpty()) {
            return;
        }
        int a = Math.min(fromShown, toShown);
        int b = Math.max(fromShown, toShown);
        a = Math.max(0, Math.min(a, shown.size() - 1));
        b = Math.max(0, Math.min(b, shown.size() - 1));
        boolean changed = false;
        for (int j = a; j <= b; j++) {
            EntityType<?> type = shown.get(j);
            if (listDragAdd) {
                if (selection.add(type)) {
                    changed = true;
                }
            } else {
                if (selection.remove(type)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            saveAction.run();
        }
    }

    private void listDragMoveToIndex(int idx) {
        if (idx < 0 || !listDragActive) {
            return;
        }
        applyListDragRange(listDragLastShownIndex, idx);
        listDragLastShownIndex = idx;
    }

    private int[] toggleFourColumnLayout(int px, int pw) {
        int innerW = pw - PAD * 2;
        int colW = (innerW - 3 * TOGGLE_INNER_GAP) / 4;
        int t1x = px + PAD;
        int t2x = t1x + colW + TOGGLE_INNER_GAP;
        int t3x = t2x + colW + TOGGLE_INNER_GAP;
        int t4x = t3x + colW + TOGGLE_INNER_GAP;
        return new int[]{t1x, colW, t2x, t3x, t4x};
    }

    private int visibleListLines(int panelHeight) {
        int listTop = listAreaTop(0);
        int listBottom = panelHeight - FOOTER_H - PAD;
        return Math.max(1, (listBottom - listTop) / LINE_H);
    }

    private int maxListScroll(int visLines) {
        return Math.max(0, shown.size() - visLines);
    }

    private String ellipsize(String s, int maxWidth) {
        if (textRenderer.getWidth(s) <= maxWidth) {
            return s;
        }
        String dots = "...";
        int dotsW = textRenderer.getWidth(dots);
        for (int len = s.length() - 1; len >= 1; len--) {
            String t = s.substring(0, len) + dots;
            if (textRenderer.getWidth(t) <= maxWidth) {
                return t;
            }
        }
        return dots;
    }

    private int clearButtonWidth() {
        return textRenderer.getWidth(CLEAR_BTN_LABEL) + 10;
    }

    private int clearButtonX(int px, int pw) {
        return px + (pw - clearButtonWidth()) / 2;
    }

    private int clearButtonY(int py) {
        return toggleRowY(py) + TOGGLE_H + AFTER_TOGGLE_CLEAR_GAP;
    }

    private void drawClearButton(DrawContext ctx, int px, int pw, int py, int mouseX, int mouseY) {
        int x = clearButtonX(px, pw);
        int y = clearButtonY(py);
        int w = clearButtonWidth();
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + CLEAR_BTN_H;
        ctx.fill(x, y, x + w, y + CLEAR_BTN_H, hov ? 0x44CDD6F4 : OVERLAY);
        ctx.fill(x, y, x + w, y + 1, OVERLAY);
        ctx.fill(x, y + CLEAR_BTN_H - 1, x + w, y + CLEAR_BTN_H, OVERLAY);
        ctx.fill(x, y, x + 1, y + CLEAR_BTN_H, OVERLAY);
        ctx.fill(x + w - 1, y, x + w, y + CLEAR_BTN_H, OVERLAY);
        int lw = textRenderer.getWidth(CLEAR_BTN_LABEL);
        ctx.drawText(textRenderer, CLEAR_BTN_LABEL, x + (w - lw) / 2, y + 3, TEAL, false);
    }

    private void drawBulkToggle(
            DrawContext ctx,
            int x,
            int y,
            int w,
            boolean allSelected,
            int mouseX,
            int mouseY,
            String label
    ) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + TOGGLE_H;
        int bg = allSelected ? 0x5594E2D5 : OVERLAY;
        ctx.fill(x, y, x + w, y + TOGGLE_H, hov ? 0x44CDD6F4 : bg);
        if (allSelected) {
            ctx.fill(x, y, x + w, y + 1, TEAL);
            ctx.fill(x, y + TOGGLE_H - 1, x + w, y + TOGGLE_H, TEAL);
            ctx.fill(x, y, x + 1, y + TOGGLE_H, TEAL);
            ctx.fill(x + w - 1, y, x + w, y + TOGGLE_H, TEAL);
        }
        int lw = textRenderer.getWidth(label);
        int col = allSelected ? TEAL : TEXT_COL;
        ctx.drawText(textRenderer, label, x + (w - lw) / 2, y + 2, col, false);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        if (now - lastBlink > 530) {
            cursorVisible = !cursorVisible;
            lastBlink = now;
        }

        int px = panelX(), pw = panelW(), py = 5, ph = height - 10;
        int visLines = visibleListLines(ph);
        int listTop = listAreaTop(py);
        int listW = pw - PAD * 2;
        int maxScroll = maxListScroll(visLines);
        listScroll = Math.max(0, Math.min(listScroll, maxScroll));

        ctx.fill(px, py, px + pw, py + ph, BG);
        ctx.fill(px, py, px + pw, py + 1, SURFACE);
        ctx.fill(px, py + ph - 1, px + pw, py + ph, SURFACE);
        ctx.fill(px, py, px + 1, py + ph, SURFACE);
        ctx.fill(px + pw - 1, py, px + pw, py + ph, SURFACE);

        ctx.fill(px, py, px + pw, py + HEADER_H, 0xFF181825);
        ctx.fill(px, py + HEADER_H - 1, px + pw, py + HEADER_H, SURFACE);
        ctx.drawTextWithShadow(textRenderer, headerTitle, px + PAD, py + 7, MAUVE);

        int sfY = py + SEARCH_TOP_OFF, sfX = px + PAD, sfW = pw - PAD * 2;
        ctx.fill(sfX, sfY, sfX + sfW, sfY + SEARCH_H, SURFACE);
        ctx.fill(sfX, sfY, sfX + sfW, sfY + 1, OVERLAY);
        ctx.fill(sfX, sfY + SEARCH_H - 1, sfX + sfW, sfY + SEARCH_H, OVERLAY);
        ctx.fill(sfX, sfY, sfX + 1, sfY + SEARCH_H, OVERLAY);
        ctx.fill(sfX + sfW - 1, sfY, sfX + sfW, sfY + SEARCH_H, OVERLAY);

        int textX = sfX + 3, textY = sfY + 3;
        if (searchQuery.isEmpty()) {
            ctx.drawText(textRenderer, "Search...", textX, textY, SUBTEXT, false);
        } else {
            ctx.drawText(textRenderer, searchQuery, textX, textY, TEXT_COL, false);
        }
        if (cursorVisible) {
            int cx = textX + textRenderer.getWidth(searchQuery.substring(0, Math.min(cursorPos, searchQuery.length())));
            ctx.fill(cx, textY - 1, cx + 1, textY + 9, TEAL);
        }

        int ty = toggleRowY(py);
        int[] tl = toggleFourColumnLayout(px, pw);
        int t1x = tl[0], colW = tl[1], t2x = tl[2], t3x = tl[3], t4x = tl[4];
        boolean mobsAll = allInCategorySelected(EntitySelectionScreen::isMonster);
        boolean passiveAll = allInCategorySelected(EntitySelectionScreen::isPassive);
        boolean miscAll = allInCategorySelected(EntitySelectionScreen::isMisc);
        boolean itemsAll = allInCategorySelected(EntitySelectionScreen::isItemEntity);
        drawBulkToggle(ctx, t1x, ty, colW, mobsAll, mouseX, mouseY, "Mobs");
        drawBulkToggle(ctx, t2x, ty, colW, passiveAll, mouseX, mouseY, "Passive");
        drawBulkToggle(ctx, t3x, ty, colW, miscAll, mouseX, mouseY, "Misc");
        drawBulkToggle(ctx, t4x, ty, colW, itemsAll, mouseX, mouseY, "Items");
        drawClearButton(ctx, px, pw, py, mouseX, mouseY);

        int endIdx = Math.min(listScroll + visLines, shown.size());
        for (int i = listScroll; i < endIdx; i++) {
            EntityType<?> type = shown.get(i);
            int row = i - listScroll;
            int ry = listTop + row * LINE_H;
            String typeId = Registries.ENTITY_TYPE.getId(type).toString();
            String displayName = Text.translatable(type.getTranslationKey()).getString();
            boolean sel = selection.contains(type);
            int rx1 = px + PAD;
            int rx2 = px + pw - PAD;
            boolean hov = mouseX >= rx1 && mouseX < rx2 && mouseY >= ry && mouseY < ry + LINE_H;

            int bg = sel ? 0x5594E2D5 : OVERLAY;
            ctx.fill(rx1, ry, rx2, ry + LINE_H - 1, bg);
            if (sel) {
                ctx.fill(rx1, ry, rx1 + 2, ry + LINE_H - 1, TEAL);
            }
            if (hov) {
                ctx.fill(rx1, ry, rx2, ry + LINE_H - 1, 0x33CDD6F4);
            }

            String line = ellipsize(displayName, listW - 4);
            int lineW = textRenderer.getWidth(line);
            int nameX = rx1 + (listW - lineW) / 2;
            ctx.drawText(textRenderer, line, nameX, ry + 3, sel ? TEAL : TEXT_COL, false);

            if (hov) {
                ctx.drawTooltip(textRenderer, List.of(
                        Text.literal(displayName).withColor(TEXT_COL),
                        Text.literal(typeId).withColor(SUBTEXT)
                ), mouseX, mouseY);
            }
        }

        String footer = selection.size() + " Selected | " + shown.size()
                + " Shown | Scroll to Move";
        int footerW = textRenderer.getWidth(footer);
        int footerX = px + (pw - footerW) / 2;
        int listBottomEdge = listTop + visLines * LINE_H;
        int panelBottom = py + ph;
        int gap = panelBottom - listBottomEdge;
        int fh = textRenderer.fontHeight;
        int footerY;
        if (gap <= fh) {
            footerY = Math.max(listBottomEdge, panelBottom - fh - PAD);
        } else {
            footerY = listBottomEdge + (gap - fh) / 2;
        }
        ctx.drawText(textRenderer, footer, footerX, footerY, SUBTEXT, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        int mx = (int) click.x(), my = (int) click.y();
        int px = panelX(), pw = panelW(), py = 5, ph = height - 10;
        int visLines = visibleListLines(ph);
        int listTop = listAreaTop(py);

        int sfY = py + SEARCH_TOP_OFF, sfX = px + PAD, sfW = pw - PAD * 2;
        if (mx >= sfX && mx < sfX + sfW && my >= sfY && my < sfY + SEARCH_H) {
            int relX = mx - (sfX + 3), best = searchQuery.length(), bestDist = Integer.MAX_VALUE;
            for (int i = 0; i <= searchQuery.length(); i++) {
                int d = Math.abs(textRenderer.getWidth(searchQuery.substring(0, i)) - relX);
                if (d < bestDist) {
                    bestDist = d;
                    best = i;
                }
            }
            cursorPos = best;
            return true;
        }

        int ty = toggleRowY(py);
        int[] tl = toggleFourColumnLayout(px, pw);
        int t1x = tl[0], colW = tl[1], t2x = tl[2], t3x = tl[3], t4x = tl[4];
        if (my >= ty && my < ty + TOGGLE_H) {
            if (mx >= t1x && mx < t1x + colW) {
                toggleCategory(EntitySelectionScreen::isMonster);
                return true;
            }
            if (mx >= t2x && mx < t2x + colW) {
                toggleCategory(EntitySelectionScreen::isPassive);
                return true;
            }
            if (mx >= t3x && mx < t3x + colW) {
                toggleCategory(EntitySelectionScreen::isMisc);
                return true;
            }
            if (mx >= t4x && mx < t4x + colW) {
                toggleCategory(EntitySelectionScreen::isItemEntity);
                return true;
            }
        }

        int cx = clearButtonX(px, pw);
        int cy = clearButtonY(py);
        int cw = clearButtonWidth();
        if (mx >= cx && mx < cx + cw && my >= cy && my < cy + CLEAR_BTN_H) {
            selection.clear();
            saveAction.run();
            return true;
        }

        int endIdx = Math.min(listScroll + visLines, shown.size());
        for (int i = listScroll; i < endIdx; i++) {
            int row = i - listScroll;
            int ry = listTop + row * LINE_H;
            int rx1 = px + PAD;
            int rx2 = px + pw - PAD;
            if (mx >= rx1 && mx < rx2 && my >= ry && my < ry + LINE_H) {
                if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    return true;
                }
                EntityType<?> type = shown.get(i);
                listDragAdd = !selection.contains(type);
                listDragActive = true;
                listDragLastShownIndex = i;
                applyListDragRange(i, i);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        if (listDragActive && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int px = panelX(), pw = panelW(), py = 5, ph = height - 10;
            int idx = listHitShownIndex((int) click.x(), (int) click.y(), px, pw, py, ph);
            if (idx >= 0) {
                listDragMoveToIndex(idx);
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        listDragActive = false;
        listDragLastShownIndex = -1;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int ph = height - 10;
        int visLines = visibleListLines(ph);
        int maxScroll = maxListScroll(visLines);
        listScroll = Math.max(0, Math.min(listScroll - (int) Math.signum(verticalAmount), maxScroll));
        if (listDragActive) {
            int px = panelX(), pw = panelW(), py = 5;
            int idx = listHitShownIndex((int) mouseX, (int) mouseY, px, pw, py, ph);
            if (idx >= 0) {
                listDragMoveToIndex(idx);
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int key = input.getKeycode();
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorPos > 0) {
                searchQuery = searchQuery.substring(0, cursorPos - 1) + searchQuery.substring(cursorPos);
                cursorPos--;
                rebuildShown();
            }
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE) {
            if (cursorPos < searchQuery.length()) {
                searchQuery = searchQuery.substring(0, cursorPos) + searchQuery.substring(cursorPos + 1);
                rebuildShown();
            }
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT && cursorPos > 0) {
            cursorPos--;
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT && cursorPos < searchQuery.length()) {
            cursorPos++;
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME) {
            cursorPos = 0;
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_END) {
            cursorPos = searchQuery.length();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        if (input.isValidChar()) {
            searchQuery = searchQuery.substring(0, cursorPos) + input.asString() + searchQuery.substring(cursorPos);
            cursorPos++;
            rebuildShown();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void close() {
        saveAction.run();
        assert client != null;
        client.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
