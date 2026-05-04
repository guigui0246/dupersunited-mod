package com.vinzy.cataddons.features.screens.mainmenu;

import com.vinzy.cataddons.features.screens.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public class UnsafeModuleWarningScreen extends Screen {

    private final Screen parent;
    private final ServerInfo serverInfo;

    public UnsafeModuleWarningScreen(Screen parent, ServerInfo serverInfo) {
        super(Text.literal("Unsafe Module Warning"));
        this.parent = parent;
        this.serverInfo = serverInfo;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§aConnect Anyway"), btn -> {
            ServerAddress address = ServerAddress.parse(serverInfo.address);
            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, address, serverInfo, false, null);
        }).dimensions(this.width / 2 - 155, this.height / 2 + 20, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§cGo Back"), btn ->
                client.setScreen(parent)
        ).dimensions(this.width / 2 + 5, this.height / 2 + 20, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Open Click GUI"), btn ->
                client.setScreen(new ClickGui(this))
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
                Text.literal("§cUnsafe Modules Active!"),
                this.width / 2, this.height / 2 - 40, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Hold on! You are about to connect to"),
                this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§f" + serverInfo.address),
                this.width / 2, this.height / 2 - 8, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7with a unsafe module enabled."),
                this.width / 2, this.height / 2 + 4, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
