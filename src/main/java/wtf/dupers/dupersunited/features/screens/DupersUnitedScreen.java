package wtf.dupers.dupersunited.features.screens;

import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.features.proxies.ProxyScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.CapeScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.KeybindScreen;
import wtf.dupers.dupersunited.features.ssidLogin.AccountsScreen;
import wtf.dupers.dupersunited.features.ssidLogin.SsidLoginScreen;
import wtf.dupers.dupersunited.utils.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DupersUnitedScreen extends Screen {

    private final Screen parent;
    private String quote;

    public DupersUnitedScreen(Screen parent) {
        super(Text.literal("DupersUnited"));
        this.parent = parent;
        this.quote = SharedVariables.randomQuote();

        AccountsScreen.preloadAccounts();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, ColorUtil.MANTLE);

        int centerX = this.width / 2;
        boolean offline = this.client.player == null;

        int btnW = 160;
        int btnH = 22;
        int gap  = 5;
        int sectionGap = 14;

        int totalHeight = (btnH + gap) * 2 - gap
                + sectionGap + 10
                + (offline ? (btnH + gap) * 2 - gap + sectionGap + 10 : 0)
                + sectionGap + 10
                + (btnH + gap) * 2 - gap;

        int y = this.height / 2 - totalHeight / 2;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(quote).withColor(ColorUtil.SUBTEXT).formatted(Formatting.ITALIC),
                centerX, 10, -1);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("DupersUnited").withColor(ColorUtil.MAUVE).formatted(Formatting.BOLD),
                centerX, 22, -1);
        context.fill(centerX - 40, 33, centerX + 40, 34, ColorUtil.LAVENDER);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("— General —").withColor(ColorUtil.FADED_NAVY), centerX, y, -1);
        y += 10;

        ButtonWidget clickGui = ButtonWidget.builder(Text.literal("Click GUI Config"), b ->
                this.client.setScreen(new ClickGui(this.client.currentScreen))
        ).dimensions(centerX - btnW / 2, y, btnW, btnH).build();
        this.addDrawableChild(clickGui);
        y += btnH + gap;

        ButtonWidget cape = ButtonWidget.builder(Text.literal("Cape Manager"), b ->
                this.client.setScreen(new CapeScreen(this.client.currentScreen))
        ).dimensions(centerX - btnW / 2, y, btnW, btnH).build();
        this.addDrawableChild(cape);
        y += btnH + sectionGap;

        if (offline) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("— Account —").withColor(ColorUtil.FADED_NAVY), centerX, y, -1);
            y += 10;

            ButtonWidget ssid = ButtonWidget.builder(Text.literal("SSID Login"), b ->
                    this.client.setScreen(new SsidLoginScreen(this.client.currentScreen))
            ).dimensions(centerX - btnW / 2, y, btnW, btnH).build();
            this.addDrawableChild(ssid);
            y += btnH + gap;

            ButtonWidget accounts = ButtonWidget.builder(Text.literal("Accounts"), b ->
                    this.client.setScreen(new AccountsScreen(this.client.currentScreen))
            ).dimensions(centerX - btnW / 2, y, btnW, btnH).build();
            this.addDrawableChild(accounts);
            y += btnH + sectionGap;
        }

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("— Network —").withColor(ColorUtil.FADED_NAVY), centerX, y, -1);
        y += 10;

        ButtonWidget proxy = ButtonWidget.builder(Text.literal("Proxy Manager"), b ->
                this.client.setScreen(new ProxyScreen(this.client.currentScreen))
        ).dimensions(centerX - btnW / 2, y, btnW, btnH).build();
        this.addDrawableChild(proxy);
        y += btnH + gap;

        ButtonWidget keybind = ButtonWidget.builder(Text.literal("Keybind Manager"), b ->
                this.client.setScreen(new KeybindScreen(this.client.currentScreen))
        ).dimensions(centerX - btnW / 2, y, btnW, btnH).build();
        this.addDrawableChild(keybind);

        ButtonWidget back = ButtonWidget.builder(Text.literal("Back"), b ->
                this.client.setScreen(parent)
        ).dimensions(centerX - btnW / 2, this.height - 25, btnW, btnH).build();
        this.addDrawableChild(back);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}