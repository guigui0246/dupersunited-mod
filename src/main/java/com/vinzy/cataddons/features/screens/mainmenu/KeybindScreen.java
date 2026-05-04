package com.vinzy.cataddons.features.screens.mainmenu;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.features.ConfigManager;
import com.vinzy.cataddons.features.chatmacros.ChatMacro;
import com.vinzy.cataddons.features.chatmacros.ChatMacroManager;
import com.vinzy.cataddons.keybinds.Keybind;
import com.vinzy.cataddons.keybinds.KeybindManager;
import com.vinzy.cataddons.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.vinzy.cataddons.utils.ColorUtil.*;

public class KeybindScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger("DU/Keybinds");

    private final Screen parent;
    private int scrollOffset = 0;

    private Module listeningModule = null;
    private Keybind listeningKeybind = null;
    private String listeningMacro = null;
    private String listeningChatMacro = null;

    private String searchQuery = "";
    private TextFieldWidget searchField;

    private static final int ROW_HEIGHT = 26;
    private static final int START_Y = 66;
    private static final int BTN_WIDTH  = 90;
    private static final int BTN_HEIGHT = 18;

    private sealed interface Row permits Row.Category, Row.ModuleRow, Row.KeybindRow, Row.MacroRow, Row.ChatMacroRow {
        record Category(String label) implements Row {
        }

        record ModuleRow(Module module) implements Row {
        }

        record KeybindRow(Keybind keybind) implements Row {
        }

        record MacroRow(String name, int key) implements Row {
        }

        record ChatMacroRow(String name, int key) implements Row {
        }
    }

    private final List<Row> rows = new ArrayList<>();

    public KeybindScreen(Screen parent) {
        super(Text.literal("Keybinds"));
        this.parent = parent;
    }

    private int panelLeft() {
        return this.width / 2 - 160;
    }

    private int panelRight() {
        return this.width / 2 + 160;
    }

    private int panelBottom()  {
        return this.height - 36;
    }

    private int visibleHeight() {
        return panelBottom() - START_Y;
    }

    private int visibleRows() {
        return visibleHeight() / ROW_HEIGHT;
    }

    private int maxScroll() {
        return Math.max(0, filteredRows().size() - visibleRows());
    }

    private boolean isListening() {
        return listeningModule != null || listeningKeybind != null || listeningMacro != null || listeningChatMacro != null;
    }


    private List<Row> filteredRows() {
        if (searchQuery.isEmpty()) return rows;
        List<Row> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (row instanceof Row.Category) {
                boolean hasMatch = false;
                for (int j = i + 1; j < rows.size(); j++) {
                    Row child = rows.get(j);
                    if (child instanceof Row.Category) break;
                    if (rowMatchesSearch(child)) { hasMatch = true; break; }
                }
                if (hasMatch) result.add(row);
            } else if (rowMatchesSearch(row)) {
                result.add(row);
            }
        }
        return result;
    }

    private boolean rowMatchesSearch(Row row) {
        return switch (row) {
            case Row.ModuleRow(Module m) -> m.getName().toLowerCase(Locale.ROOT).contains(searchQuery);
            case Row.KeybindRow(Keybind kb) -> kb.getId().toLowerCase(Locale.ROOT).contains(searchQuery);
            case Row.MacroRow(String name, int k) -> name.toLowerCase(Locale.ROOT).contains(searchQuery);
            case Row.ChatMacroRow(String name, int k) -> name.toLowerCase(Locale.ROOT).contains(searchQuery);
            default -> false;
        };
    }

    @Override
    protected void init() {
        rows.clear();
        scrollOffset = 0;
        searchQuery = "";
        listeningModule = null;
        listeningKeybind = null;
        listeningMacro = null;
        listeningChatMacro = null;

        Map<String, List<Module>> grouped = new LinkedHashMap<>();
        for (Module m : MainClient.MODULE_MANAGER.getModules()) {
            grouped.computeIfAbsent(m.getCategory(), k -> new ArrayList<>()).add(m);
        }
        for (var entry : grouped.entrySet()) {
            rows.add(new Row.Category(entry.getKey()));
            for (Module m : entry.getValue()) {
                rows.add(new Row.ModuleRow(m));
            }
        }

        Set<String> chatMacroIds = ChatMacroManager.getMacros().keySet();
        List<Keybind> filteredKeybinds = KeybindManager.getRegisteredKeybinds().values().stream()
            .filter(kb -> !chatMacroIds.contains(kb.getId()))
            .collect(Collectors.toList());
        if (!filteredKeybinds.isEmpty()) {
            rows.add(new Row.Category("Keybinds"));
            for (Keybind kb : filteredKeybinds) rows.add(new Row.KeybindRow(kb));
        }

        if (!ChatMacroManager.getMacros().isEmpty()) {
            rows.add(new Row.Category("Chat Macros"));
            for (ChatMacro cm : ChatMacroManager.getMacros().values()) {
                rows.add(new Row.ChatMacroRow(cm.getName(), cm.getKeyCode()));
            }
        }

        int searchX = panelLeft() + 4;
        int searchW = panelRight() - panelLeft() - 8;
        searchField = new TextFieldWidget(textRenderer, searchX, 41, searchW, 18, Text.literal("Search..."));
        searchField.setMaxLength(64);
        searchField.setSuggestion("Search keybinds...");
        searchField.setChangedListener(text -> {
            searchQuery = text.toLowerCase(Locale.ROOT);
            searchField.setSuggestion(text.isEmpty() ? "Search keybinds..." : "");
            scrollOffset = 0;
            rebuildButtons();
        });
        this.addDrawableChild(searchField);

        rebuildButtons();
    }


    private void rebuildButtons() {
        this.clearChildren();

        if (searchField != null) this.addDrawableChild(searchField);

        List<Row> fr = filteredRows();
        int visible  = visibleRows();

        for (int i = 0; i < fr.size(); i++) {
            Row row  = fr.get(i);
            int screenI = i - scrollOffset;
            if (screenI < 0 || screenI >= visible) continue;

            int y = START_Y + screenI * ROW_HEIGHT;
            int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;
            int btnX = panelRight() - BTN_WIDTH - 6;
            int clearX = panelRight() - BTN_WIDTH - 28;

            if (row instanceof Row.ModuleRow(Module module)) {
                boolean listening = module == listeningModule;
                this.addDrawableChild(ButtonWidget.builder(
                    listening ? Text.literal("[ press a key ]") : getKeyText(module.getKeybind()),
                    btn -> {
                        listeningModule = (listeningModule == module) ? null : module;
                        listeningKeybind = null;
                        listeningMacro = null;
                        listeningChatMacro = null;
                        rebuildButtons();
                    }
                ).dimensions(btnX, btnY, BTN_WIDTH, BTN_HEIGHT).build());

                if (module.getKeybind() != GLFW.GLFW_KEY_UNKNOWN && !listening) {
                    this.addDrawableChild(ButtonWidget.builder(Text.literal("§cx"), btn -> {
                        module.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
                        ConfigManager.save();
                        rebuildButtons();
                    }).dimensions(clearX, btnY, 18, BTN_HEIGHT).build());
                }

            } else if (row instanceof Row.KeybindRow(Keybind keybind)) {
                boolean listening = keybind == listeningKeybind;
                this.addDrawableChild(ButtonWidget.builder(
                    listening ? Text.literal("[ press a key ]") : getKeyText(keybind.getKeyCode()),
                    btn -> {
                        listeningKeybind = (listeningKeybind == keybind) ? null : keybind;
                        listeningModule = null;
                        listeningMacro = null;
                        listeningChatMacro = null;
                        rebuildButtons();
                    }
                ).dimensions(btnX, btnY, BTN_WIDTH, BTN_HEIGHT).build());

                if (keybind.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN && !listening) {
                    this.addDrawableChild(ButtonWidget.builder(Text.literal("§cx"), btn -> {
                        keybind.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
                        ConfigManager.save();
                        rebuildButtons();
                    }).dimensions(clearX, btnY, 18, BTN_HEIGHT).build());
                }

            } else if (row instanceof Row.MacroRow(String name, int key)) {
                boolean listening = name.equals(listeningMacro);
                this.addDrawableChild(ButtonWidget.builder(
                    listening ? Text.literal("[ press a key ]") : getKeyText(key),
                    btn -> {
                        listeningMacro = listening ? null : name;
                        listeningModule = null;
                        listeningKeybind = null;
                        listeningChatMacro = null;
                        rebuildButtons();
                    }
                ).dimensions(btnX, btnY, BTN_WIDTH, BTN_HEIGHT).build());

                if (key != GLFW.GLFW_KEY_UNKNOWN && !listening) {
                    this.addDrawableChild(ButtonWidget.builder(Text.literal("§cx"), btn -> {
                        //saveMacroKey(name, GLFW.GLFW_KEY_UNKNOWN);
                        init();
                    }).dimensions(clearX, btnY, 18, BTN_HEIGHT).build());
                }

            } else if (row instanceof Row.ChatMacroRow(String name, int key)) {
                boolean listening = name.equals(listeningChatMacro);
                this.addDrawableChild(ButtonWidget.builder(
                    listening ? Text.literal("[ press a key ]") : getKeyText(key),
                    btn -> {
                        listeningChatMacro = listening ? null : name;
                        listeningModule = null;
                        listeningKeybind = null;
                        listeningMacro = null;
                        rebuildButtons();
                    }
                ).dimensions(btnX, btnY, BTN_WIDTH, BTN_HEIGHT).build());

                if (key != GLFW.GLFW_KEY_UNKNOWN && !listening) {
                    this.addDrawableChild(ButtonWidget.builder(Text.literal("§cx"), btn -> {
                        ChatMacroManager.rebind(name, GLFW.GLFW_KEY_UNKNOWN);
                        init();
                    }).dimensions(clearX, btnY, 18, BTN_HEIGHT).build());
                }
            }
        }

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            btn -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset - (int) vertical));
        rebuildButtons();
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (isListening()) {
            int mx = (int) click.x();
            int my = (int) click.y();
            int button = click.button();
            boolean insidePanel = mx >= panelLeft() && mx <= panelRight()
                && my >= START_Y && my <= panelBottom();

            if (!insidePanel) {
                listeningModule = null;
                listeningKeybind = null;
                listeningMacro = null;
                listeningChatMacro = null;
                rebuildButtons();
                return true;
            }

            if (listeningModule != null) {
                listeningModule.setKeybind(button);
                ConfigManager.save();
                listeningModule = null;
            } else if (listeningKeybind != null) {
                listeningKeybind.setKeyCode(button);
                ConfigManager.save();
                listeningKeybind = null;
            } else if (listeningChatMacro != null) {
                ChatMacroManager.rebind(listeningChatMacro, button);
                listeningChatMacro = null;
            }

            init();
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (listeningModule != null) {
            int keyCode = input.key();
            listeningModule.setKeybind(keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            ConfigManager.save();
            listeningModule = null;
            rebuildButtons();
            return true;
        }
        if (listeningKeybind != null) {
            int keyCode = input.key();
            listeningKeybind.setKeyCode(keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            ConfigManager.save();
            listeningKeybind = null;
            rebuildButtons();
            return true;
        }
        if (listeningChatMacro != null) {
            int keyCode = input.key();
            ChatMacroManager.rebind(listeningChatMacro,
                keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            listeningChatMacro = null;
            init();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, DEEP_SAPPHIRE);

        ctx.fill(0, 0, this.width, 36, MANTLE);
        ctx.fill(0, 36, this.width, 37, FADED_INDIGO);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("Keybinds").formatted(Formatting.BOLD),
            this.width / 2, 14, LAVENDER);

        ctx.fill(panelLeft(), 38, panelRight(), 62, DEEP_INDIGO);
        ctx.fill(panelLeft(), 62, panelRight(), 63, FADED_INDIGO);

        ctx.fill(panelLeft(), START_Y, panelRight(), panelBottom(), MANTLE);

        List<Row> fr = filteredRows();
        int visible = visibleRows();

        for (int i = 0; i < fr.size(); i++) {
            Row row = fr.get(i);
            int screenI = i - scrollOffset;
            if (screenI < 0 || screenI >= visible) continue;

            int y = START_Y + screenI * ROW_HEIGHT;

            if (row instanceof Row.Category(String label)) {
                ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);
                ctx.fill(panelLeft(), y + ROW_HEIGHT - 1, panelRight(), y + ROW_HEIGHT, FADED_INDIGO);
                ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal("▸ " + label.toUpperCase()),
                    panelLeft() + 8, y + (ROW_HEIGHT - 9) / 2, MAUVE);

            } else if (row instanceof Row.ModuleRow(Module module)) {
                boolean listening = module == listeningModule;
                boolean hovered   = mouseX >= panelLeft() && mouseX <= panelRight()
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

                if (hovered || listening) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);
                else if (i % 2 == 0) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, 0x08FFFFFF);

                ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(module.getName()),
                    panelLeft() + 10, y + (ROW_HEIGHT - 9) / 2,
                    listening ? PEACH : PALE_NAVY);

                boolean hasBind = module.getKeybind() != GLFW.GLFW_KEY_UNKNOWN;
                ctx.fill(panelLeft() + 3, y + ROW_HEIGHT / 2 - 2,
                    panelLeft() + 5, y + ROW_HEIGHT / 2 + 2,
                    listening ? PEACH : hasBind ? GREEN : FADED_INDIGO);
                ctx.fill(panelLeft(), y + ROW_HEIGHT - 1, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);

            } else if (row instanceof Row.KeybindRow(Keybind keybind)) {
                boolean listening = keybind == listeningKeybind;
                boolean hovered   = mouseX >= panelLeft() && mouseX <= panelRight()
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

                if (hovered || listening) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);
                else if (i % 2 == 0) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, 0x08FFFFFF);

                ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(keybind.getId()),
                    panelLeft() + 10, y + (ROW_HEIGHT - 9) / 2,
                    listening ? PEACH : PALE_NAVY);

                boolean hasBind = keybind.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN;
                ctx.fill(panelLeft() + 3, y + ROW_HEIGHT / 2 - 2,
                    panelLeft() + 5, y + ROW_HEIGHT / 2 + 2,
                    listening ? PEACH : hasBind ? GREEN : FADED_INDIGO);
                ctx.fill(panelLeft(), y + ROW_HEIGHT - 1, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);

            } else if (row instanceof Row.MacroRow(String name, int key)) {
                boolean listening = name.equals(listeningMacro);
                boolean hovered   = mouseX >= panelLeft() && mouseX <= panelRight()
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

                if (hovered || listening) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);
                else if (i % 2 == 0) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, 0x08FFFFFF);

                ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(name),
                    panelLeft() + 10, y + (ROW_HEIGHT - 9) / 2,
                    listening ? PEACH : PALE_NAVY);

                boolean hasBind = key != GLFW.GLFW_KEY_UNKNOWN;
                ctx.fill(panelLeft() + 3, y + ROW_HEIGHT / 2 - 2,
                    panelLeft() + 5, y + ROW_HEIGHT / 2 + 2,
                    listening ? PEACH : hasBind ? GREEN : FADED_INDIGO);
                ctx.fill(panelLeft(), y + ROW_HEIGHT - 1, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);

            } else if (row instanceof Row.ChatMacroRow(String name, int key)) {
                boolean listening = name.equals(listeningChatMacro);
                boolean hovered   = mouseX >= panelLeft() && mouseX <= panelRight()
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

                if (hovered || listening) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);
                else if (i % 2 == 0) ctx.fill(panelLeft(), y, panelRight(), y + ROW_HEIGHT, 0x08FFFFFF);

                ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(name),
                    panelLeft() + 10, y + (ROW_HEIGHT - 9) / 2,
                    listening ? PEACH : PALE_NAVY);

                boolean hasBind = key != GLFW.GLFW_KEY_UNKNOWN;
                ctx.fill(panelLeft() + 3, y + ROW_HEIGHT / 2 - 2,
                    panelLeft() + 5, y + ROW_HEIGHT / 2 + 2,
                    listening ? PEACH : hasBind ? GREEN : FADED_INDIGO);
                ctx.fill(panelLeft(), y + ROW_HEIGHT - 1, panelRight(), y + ROW_HEIGHT, DEEP_INDIGO);
            }
        }

        if (maxScroll() > 0) {
            int totalH = visibleHeight();
            int barH   = Math.max(24, totalH * visible / fr.size());
            int barY   = START_Y + (totalH - barH) * scrollOffset / maxScroll();
            ctx.fill(panelRight() + 2, START_Y, panelRight() + 4, panelBottom(), DEEP_INDIGO);
            ctx.fill(panelRight() + 2, barY, panelRight() + 4, barY + barH, LAVENDER);
        }

        if (fr.isEmpty() && !searchQuery.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("No keybinds match \"" + searchField.getText() + "\""),
                this.width / 2, START_Y + visibleHeight() / 2, FADED_NAVY);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }


    private Text getKeyText(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return Text.literal("unbound").formatted(Formatting.WHITE);
        if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            String name = switch (key) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "MOUSE LEFT";
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "MOUSE RIGHT";
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "MOUSE MIDDLE";
                default -> "MOUSE " + (key + 1);
            };
            return Text.literal(name);
        }

        if (key < 0) {
            int btn = (-key) - 100;
            if (btn >= GLFW.GLFW_MOUSE_BUTTON_1 && btn <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                String name = switch (btn) {
                    case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "MOUSE LEFT";
                    case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "MOUSE RIGHT";
                    case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "MOUSE MIDDLE";
                    default -> "MOUSE " + (btn + 1);
                };
                return Text.literal(name);
            }
            return Text.literal("UNKNOWN");
        }
        return Text.literal(InputUtil.Type.KEYSYM.createFromCode(key).getLocalizedText().getString().toUpperCase());
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}