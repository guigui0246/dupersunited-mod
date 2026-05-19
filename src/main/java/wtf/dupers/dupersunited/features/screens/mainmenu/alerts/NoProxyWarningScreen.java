package wtf.dupers.dupersunited.features.screens.mainmenu.alerts;

import wtf.dupers.dupersunited.features.proxies.AccountProxyLinks;
import wtf.dupers.dupersunited.features.proxies.ProxyConfigManager;
import wtf.dupers.dupersunited.features.proxies.ProxyScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class NoProxyWarningScreen extends Screen {

    private final Screen parent;
    private final ServerInfo serverInfo;

    public NoProxyWarningScreen(Screen parent, ServerInfo serverInfo) {
        super(Text.literal("No Proxy Warning"));
        this.parent = parent;
        this.serverInfo = serverInfo;
    }

    @Override
    protected void init() {
        String currentAccount = MinecraftClient.getInstance().getSession().getUsername();
        boolean canBypass = AccountProxyLinks.hasBypass(currentAccount);

        if (canBypass) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("§aConnect Anyway"), btn -> {
                ServerAddress address = ServerAddress.parse(serverInfo.address);
                ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, address, serverInfo, false, null);
            }).dimensions(this.width / 2 - 155, this.height / 2 + 20, 150, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("§cGo Back"), btn ->
                    client.setScreen(parent)
            ).dimensions(this.width / 2 + 5, this.height / 2 + 20, 150, 20).build());
        } else {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("§cGo Back"), btn ->
                    client.setScreen(parent)
            ).dimensions(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Open Proxy Manager"), btn ->
                client.setScreen(new ProxyScreen(this))
        ).dimensions(this.width / 2 - 100, this.height / 2 + 45, 200, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§cNo Proxy Active!"),
                this.width / 2, this.height / 2 - 50, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7You are about to connect to §f§l" + serverInfo.address),
                this.width / 2, this.height / 2 - 30, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7without a proxy enabled, are you sure about this?"),
                this.width / 2, this.height / 2 - 18, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(ProxyConfigManager.getWarningReason()),
                this.width / 2, this.height / 2 - 5, 0xFFFFFFFF);

        // lt the user know why there's no connect anyway button
        String currentAccount = MinecraftClient.getInstance().getSession().getUsername();
        if (!AccountProxyLinks.hasBypass(currentAccount)) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§8Enable bypass in Account Manager to connect without a proxy."),
                    this.width / 2, this.height / 2 + 8, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}