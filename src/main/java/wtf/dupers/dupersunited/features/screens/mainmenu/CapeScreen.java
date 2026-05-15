package wtf.dupers.dupersunited.features.screens.mainmenu;

import wtf.dupers.dupersunited.features.CapeManager;
import wtf.dupers.dupersunited.features.auth.AuthManager;
import wtf.dupers.dupersunited.features.auth.AuthManager.MinecraftAccount;
import wtf.dupers.dupersunited.features.ssidLogin.AccountsScreen;
import wtf.dupers.dupersunited.features.ssidLogin.SessionManager;
import wtf.dupers.dupersunited.utils.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CapeScreen extends Screen {
    private final Screen parent;
    private final List<ButtonWidget> capeButtons = new ArrayList<>();

    private ButtonWidget actionButton;
    private int scrollOffset;
    private Set<String> lastCapes = Set.of();
    private String lastSelectedCape;
    private String lastLinkedUuid;
    private Boolean lastSessionValid;

    public CapeScreen(Screen parent) {
        super(Text.literal("Cape Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AuthManager.init();

        int centerX = this.width / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), btn -> close())
                .dimensions(5, 8, 50, 20)
                .build());

        actionButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), btn -> {
                    if (SessionManager.isSessionValid()) {
                        if (AuthManager.canUseCapes()) {
                            AuthManager.requestCapeList();
                        } else {
                            AuthManager.retryAccountVerification();
                        }
                    } else if (SessionManager.isSessionValid != null) {
                        this.client.setScreen(new AccountsScreen(this));
                    } else {
                        AuthManager.retry();
                    }
                })
                .dimensions(centerX - 48, 44, 96, 20)
                .build());

        AuthManager.retry();

        rebuildCapeButtons();
        refreshButtons();
    }

    @Override
    public void tick() {
        super.tick();

        MinecraftAccount linked = AuthManager.getLinkedAccount();
        String linkedUuid = linked == null ? null : linked.uuid();
        AuthManager.CapeCatalog capeCatalog = AuthManager.getCapeCatalog();
        Boolean sessionValid = SessionManager.isSessionValid;

        if (!Objects.equals(lastLinkedUuid, linkedUuid)
                || !Objects.equals(lastSelectedCape, capeCatalog.selected())
                || !lastCapes.equals(capeCatalog.textureUrls().keySet())
                || !Objects.equals(lastSessionValid, sessionValid)) {
            rebuildCapeButtons();
        }

        refreshButtons();
    }

    private void refreshButtons() {
        if (SessionManager.isSessionValid == null) {
            actionButton.active = false;
            actionButton.setMessage(Text.literal("Validating..."));
        } else if (!SessionManager.isSessionValid) {
            actionButton.active = true;
            actionButton.setMessage(Text.literal("Accounts"));
        } else {
            actionButton.active = AuthManager.isSocketConnected();
            actionButton.setMessage(Text.literal(AuthManager.canUseCapes() ? "Refresh" : "Verify"));
        }

        for (ButtonWidget button : capeButtons) {
            button.active = AuthManager.canUseCapes();
        }
    }

    private void rebuildCapeButtons() {
        capeButtons.forEach(this::remove);
        capeButtons.clear();

        MinecraftAccount linked = AuthManager.getLinkedAccount();
        lastLinkedUuid = linked == null ? null : linked.uuid();
        AuthManager.CapeCatalog capeCatalog = AuthManager.getCapeCatalog();
        lastCapes = AuthManager.canUseCapes() ? capeCatalog.textureUrls().keySet() : Set.of();
        lastSelectedCape = capeCatalog.selected();
        lastSessionValid = SessionManager.isSessionValid;

        List<String> options = new ArrayList<>();
        if (AuthManager.canUseCapes()) {
            options.add("disabled");
            options.addAll(lastCapes);
        }

        int startY = getListTop();
        int rows = Math.max(1, (this.height - startY - 44) / 24);
        int maxOffset = Math.max(0, options.size() - rows);
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }

        int end = Math.min(options.size(), scrollOffset + rows);
        for (int i = scrollOffset; i < end; i++) {
            String capeKey = options.get(i);
            String selected = lastSelectedCape == null || lastSelectedCape.isBlank() ? "disabled" : lastSelectedCape;
            String prefix = selected.equals(capeKey) ? "> " : "";
            ButtonWidget button = ButtonWidget.builder(Text.literal(prefix + capeKey), btn ->
                            AuthManager.pickCape("disabled".equals(capeKey) ? null : capeKey))
                    .dimensions(this.width / 2 - 72, startY + ((i - scrollOffset) * 24), 172, 20)
                    .build();
            this.addDrawableChild(button);
            capeButtons.add(button);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<String> options = new ArrayList<>();
        if (AuthManager.canUseCapes()) {
            options.add("disabled");
            options.addAll(AuthManager.getCapeCatalog().textureUrls().keySet());
        }

        int rows = Math.max(1, (this.height - getListTop() - 44) / 24);
        int maxOffset = Math.max(0, options.size() - rows);
        if (maxOffset == 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (verticalAmount < 0 && scrollOffset < maxOffset) {
            scrollOffset++;
            rebuildCapeButtons();
            refreshButtons();
            return true;
        }

        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            rebuildCapeButtons();
            refreshButtons();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int getListTop() {
        if (SessionManager.isSessionValid == null || !SessionManager.isSessionValid) {
            return 126;
        }

        if (!AuthManager.canUseCapes() || AuthManager.getCapeCatalog().textureUrls().isEmpty()) {
            return 114;
        }

        return 82;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, ColorUtil.MANTLE);

        int centerX = this.width / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Cape Manager"), centerX, 16, ColorUtil.PALE_NAVY);

        MinecraftAccount linked = AuthManager.getLinkedAccount();
        if (linked == null || linked.username() == null || linked.username().isBlank()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Minecraft account not linked.").withColor(ColorUtil.RED),
                    centerX,
                    30,
                    -1
            );
        } else {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Linked Minecraft account: ").withColor(ColorUtil.FADED_NAVY)
                            .append(Text.literal(linked.username()).withColor(ColorUtil.GREEN)),
                    centerX,
                    30,
                    -1
            );
        }

        String detail = AuthManager.getDetailLine();
        if (detail != null
                && !detail.isBlank()
                && !(linked != null && linked.username() != null && detail.equals("Linked Minecraft account: " + linked.username()))) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(detail).withColor(ColorUtil.SUBTEXT),
                    centerX,
                    66,
                    -1
            );
        }

        if (SessionManager.isSessionValid == null) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Validating the current Minecraft session before cape management is enabled.")
                            .withColor(ColorUtil.YELLOW),
                    centerX,
                    92,
                    -1
            );
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Wait a moment or switch accounts if this never resolves.").withColor(ColorUtil.SUBTEXT),
                    centerX,
                    104,
                    -1
            );
        } else if (!SessionManager.isSessionValid) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("This session is not valid, so the addon does not know which account to manage capes for.")
                            .withColor(ColorUtil.YELLOW),
                    centerX,
                    92,
                    -1
            );
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Open Accounts and switch to a valid account first.").withColor(ColorUtil.SUBTEXT),
                    centerX,
                    104,
                    -1
            );
        } else if (!AuthManager.canUseCapes()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Verify this Minecraft account on the public socket to manage capes.")
                            .withColor(ColorUtil.YELLOW),
                    centerX,
                    92,
                    -1
            );
        } else if (AuthManager.getCapeCatalog().textureUrls().isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("No capes found for this account yet.").withColor(ColorUtil.YELLOW),
                    centerX,
                    92,
                    -1
            );
        }

        String selected = AuthManager.getCapeCatalog().selected();
        String selectedText = selected == null || selected.isBlank() ? "disabled" : selected;
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Selected cape: ").withColor(ColorUtil.FADED_NAVY)
                        .append(Text.literal(selectedText).withColor(ColorUtil.MAUVE)),
                centerX,
                this.height - 28,
                -1
        );

        if (AuthManager.canUseCapes()) {
            List<String> options = new ArrayList<>();
            options.add("disabled");
            options.addAll(AuthManager.getCapeCatalog().textureUrls().keySet());
            int startY = getListTop();
            int rows = Math.max(1, (this.height - startY - 44) / 24);
            int end = Math.min(options.size(), scrollOffset + rows);
            for (int i = scrollOffset; i < end; i++) {
                String capeKey = options.get(i);
                int y = startY + ((i - scrollOffset) * 24);
                renderCapePreview(context, capeKey, this.width / 2 - 98, y + 2);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCapePreview(DrawContext context, String capeKey, int x, int y) {
        context.fill(x, y, x + 18, y + 18, ColorUtil.DEEP_INDIGO);
        context.fill(x, y, x + 18, y + 1, ColorUtil.FADED_INDIGO);
        context.fill(x, y + 17, x + 18, y + 18, ColorUtil.FADED_INDIGO);
        context.fill(x, y, x + 1, y + 18, ColorUtil.FADED_INDIGO);
        context.fill(x + 17, y, x + 18, y + 18, ColorUtil.FADED_INDIGO);

        if ("disabled".equals(capeKey)) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("-"), x + 9, y + 5, ColorUtil.FADED_NAVY);
            return;
        }

        Identifier texture = CapeManager.getPreviewTexture(capeKey);
        if (texture == null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("?"), x + 9, y + 5, ColorUtil.YELLOW);
            return;
        }

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x + ((18 - 10) / 2),
                y + ((18 - 16) / 2),
                1,
                1,
                10,
                16,
                64,
                32
        );
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
