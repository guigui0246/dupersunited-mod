package com.vinzy.cataddons.features.proxies;

import com.vinzy.cataddons.features.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import static com.vinzy.cataddons.utils.ColorUtil.*;

import java.util.ArrayList;
import java.util.List;

public class ProxyScreen extends Screen {

    private static final int ENTRY_HEIGHT = 30;
    private static final int LIST_TOP = 50;
    private static final int BOTTOM_PANEL_HEIGHT = 160;

    private final Screen parent;
    private final List<ButtonWidget> entryButtons = new ArrayList<>();

    private TextFieldWidget nameField, addressField, userField, passField;
    private ButtonWidget toggleButton;
    private ButtonWidget warningButton;
    private String errorMessage = "";

    private int listBottom;
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    public ProxyScreen(Screen parent) {
        super(Text.literal("Proxy Manager"));
        this.parent = parent;
    }

    private int getMaxVisible() {
        return Math.max(1, (listBottom - LIST_TOP) / ENTRY_HEIGHT);
    }

    @Override
    protected void init() {
        listBottom = this.height - BOTTOM_PANEL_HEIGHT - 10;
        int cx = this.width / 2 - 100;

        nameField = field(cx, height - 145);
        addressField = field(cx + 105, height - 145);
        userField = field(cx, height - 120);
        passField = field(cx + 105, height - 120);

        addDrawableChild(nameField);
        addDrawableChild(addressField);
        addDrawableChild(userField);
        addDrawableChild(passField);

        // add and delet
        addDrawableChild(ButtonWidget.builder(Text.literal("§aAdd Profile"), btn -> addProfile())
            .dimensions(cx, height - 95, 97, 20)
            .tooltip(Tooltip.of(Text.literal("Add a new proxy profile")))
            .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§cDelete Profile"), btn -> deleteSelected())
            .dimensions(cx + 103, height - 95, 97, 20)
            .tooltip(Tooltip.of(Text.literal("Delete the selected proxy profile")))
            .build());

        // togle
        toggleButton = ButtonWidget.builder(toggleText(), btn -> {
                ProxyConfigManager.globalEnabled = !ProxyConfigManager.globalEnabled;
                ProxyConfigManager.save();
                btn.setMessage(toggleText());
            })
            .dimensions(cx, height - 70, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Toggles proxies on or off")))
            .build();
        addDrawableChild(toggleButton);

        // proxy warning toggle
        warningButton = ButtonWidget.builder(warningText(), btn -> {
                ProxyConfigManager.proxyWarningEnabled = !ProxyConfigManager.proxyWarningEnabled;
                ProxyConfigManager.save();
                btn.setMessage(warningText());
            })
            .dimensions(cx, height - 45, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Warns you when joining a server without a proxy enabled")))
            .build();
        addDrawableChild(warningButton);

        // back
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), btn -> client.setScreen(parent))
            .dimensions(this.width / 2 - 50, this.height - 22, 100, 18)
            .build());

        setInitialFocus(nameField);
        rebuildEntryButtons();
    }

    private TextFieldWidget field(int x, int y) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, 95, 20, Text.empty());
        f.setMaxLength(64);
        return f;
    }

    private void addProfile() {
        String name = nameField.getText().trim();
        String addr = addressField.getText().trim();
        if (name.isEmpty() || addr.isEmpty()) return;

        if (ProxyConfigManager.profiles.stream().anyMatch(p -> p.name.equalsIgnoreCase(name))) {
            //MainClient.LOGGER.info("profile alr exists, aborting creation and showing error"); workd
            errorMessage = "§cERROR: Can't create proxy with the same name as existing proxy!";
            return;
        }
        errorMessage = "";

        ProxyConfigManager.profiles.add(new ProxyProfiles(name, addr, userField.getText().trim(), passField.getText().trim()));
        ProxyConfigManager.save();

        nameField.setText("");
        addressField.setText("");
        userField.setText("");
        passField.setText("");
        rebuildEntryButtons();
    }

    private void deleteSelected() {
        List<ProxyProfiles> profiles = ProxyConfigManager.profiles;
        if (selectedIndex < 0 || selectedIndex >= profiles.size()) return;

        if (profiles.get(selectedIndex).name.equals(ProxyConfigManager.activeProfileName))
            ProxyConfigManager.activeProfileName = "";

        profiles.remove(selectedIndex);
        ProxyConfigManager.save();
        selectedIndex = -1;
        rebuildEntryButtons();
    }

    private void rebuildEntryButtons() {
        entryButtons.forEach(this::remove);
        entryButtons.clear();

        int maxVisible = getMaxVisible();
        List<ProxyProfiles> profiles = ProxyConfigManager.profiles;

        for (int i = 0; i < maxVisible && (i + scrollOffset) < profiles.size(); i++) {
            int index = i + scrollOffset;
            int y = LIST_TOP + i * ENTRY_HEIGHT;

            ButtonWidget selectBtn = ButtonWidget.builder(Text.empty(), b -> selectedIndex = index)
                .dimensions(0, y, this.width - 60, ENTRY_HEIGHT)
                .build();
            selectBtn.setAlpha(0f);
            addDrawableChild(selectBtn);
            entryButtons.add(selectBtn);

            ButtonWidget useBtn = ButtonWidget.builder(Text.literal("§aUse"), b -> {
                    ProxyConfigManager.activeProfileName = profiles.get(index).name;
                    ProxyConfigManager.save();
                    selectedIndex = index;
                })
                .dimensions(this.width - 55, y + (ENTRY_HEIGHT / 2 - 10), 50, 20)
                .tooltip(Tooltip.of(Text.literal("Set as active proxy")))
                .build();
            addDrawableChild(useBtn);
            entryButtons.add(useBtn);
        }
    }

    private Text toggleText() {
        return Text.literal(ProxyConfigManager.globalEnabled ? "§fProxies are now §aEnabled" : "§fProxies are now §cDisabled");
    }

    private Text warningText() {
        return Text.literal(ProxyConfigManager.proxyWarningEnabled ? "§fProxy Warning §aEnabled" : "§fProxy Warning §cDisabled");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, ProxyConfigManager.profiles.size() - getMaxVisible());
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        rebuildEntryButtons();
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // base background
        context.fill(0, 0, this.width, this.height, DEEP_SAPPHIRE);

        // top headr bar
        context.fill(0, 0, this.width, 42, MANTLE);
        context.fill(0, 42, this.width, 43, FADED_INDIGO);

        // bottom footer bar
        context.fill(0, height - BOTTOM_PANEL_HEIGHT, this.width, this.height, MANTLE);
        context.fill(0, height - BOTTOM_PANEL_HEIGHT - 1, this.width, height - BOTTOM_PANEL_HEIGHT, FADED_INDIGO);

        renderProxyList(context, mouseX, mouseY);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Proxy Manager").formatted(Formatting.BOLD), this.width / 2, 15, LAVENDER);

        if (!errorMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(errorMessage), this.width / 2, 28, RED);
        }

        super.render(context, mouseX, mouseY, delta);

        renderPlaceholders(context);
        toggleButton.setMessage(toggleText());
        warningButton.setMessage(warningText());
    }

    private void renderProxyList(DrawContext context, int mouseX, int mouseY) {
        List<ProxyProfiles> profiles = ProxyConfigManager.profiles;
        int maxVisible = getMaxVisible();

        for (int i = 0; i < maxVisible && (i + scrollOffset) < profiles.size(); i++) {
            int index = i + scrollOffset;
            ProxyProfiles profile = profiles.get(index);
            int y = LIST_TOP + i * ENTRY_HEIGHT;

            boolean isSelected = index == selectedIndex;
            boolean isActive = profile.name.equals(ProxyConfigManager.activeProfileName);
            boolean hovered = mouseX >= 0 && mouseX < this.width - 60 && mouseY >= y && mouseY < y + ENTRY_HEIGHT;

            int rowBg = (index % 2 == 0) ? DEEP_INDIGO : MANTLE;
            context.fill(5, y, this.width - 60, y + ENTRY_HEIGHT - 1, rowBg);

            if (isSelected) context.fill(5, y, this.width - 60, y + ENTRY_HEIGHT - 1, 0x44FFFFFF);
            else if (hovered) context.fill(5, y, this.width - 60, y + ENTRY_HEIGHT - 1, 0x22FFFFFF);

            context.fill(5, y, 7, y + ENTRY_HEIGHT - 1, isActive ? GREEN : FADED_INDIGO);

            String color = isActive ? "§a" : isSelected ? "§e" : "§f";
            context.drawTextWithShadow(textRenderer, Text.literal(color + profile.name), 13, y + 5, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, Text.literal("§7" + profile.address), 13, y + 17, 0xFFFFFFFF);
        }

        if (profiles.size() > maxVisible) {
            int totalListArea = maxVisible * ENTRY_HEIGHT;
            int barHeight = Math.max(10, totalListArea * maxVisible / profiles.size());
            int maxScroll = profiles.size() - maxVisible;
            int barY = LIST_TOP + (totalListArea - barHeight) * scrollOffset / Math.max(1, maxScroll);
            context.fill(this.width - 4, LIST_TOP, this.width - 1, LIST_TOP + totalListArea, DEEP_INDIGO);
            context.fill(this.width - 4, barY, this.width - 1, barY + barHeight, LAVENDER);
        }
    }

    private void renderPlaceholders(DrawContext context) {
        int fx = this.width / 2 - 96;
        if (nameField.getText().isEmpty()) context.drawTextWithShadow(textRenderer, Text.literal("Profile Name"), fx, height - 141, 0xAAFFFFFF);
        if (addressField.getText().isEmpty()) context.drawTextWithShadow(textRenderer, Text.literal("IP:Port"), fx + 105, height - 141, 0xAAFFFFFF);
        if (userField.getText().isEmpty()) context.drawTextWithShadow(textRenderer, Text.literal("Username"), fx, height - 116, 0xAAFFFFFF);
        if (passField.getText().isEmpty()) context.drawTextWithShadow(textRenderer, Text.literal("Password"), fx + 105, height - 116, 0xAAFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}