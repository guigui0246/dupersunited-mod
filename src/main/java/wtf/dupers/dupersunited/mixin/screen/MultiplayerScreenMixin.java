package wtf.dupers.dupersunited.mixin.screen;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.commands.subcommands.DupeCommand;
import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.features.ServerAlertConfig;
import wtf.dupers.dupersunited.features.proxies.ProxyConfigManager;
import wtf.dupers.dupersunited.features.proxies.ProxyProfiles;
import wtf.dupers.dupersunited.features.screens.mainmenu.alerts.HallOfFame;
import wtf.dupers.dupersunited.features.screens.mainmenu.alerts.HallOfShame;
import wtf.dupers.dupersunited.features.screens.DupersUnitedScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.alerts.NoProxyWarningScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.alerts.UnsafeModuleWarningScreen;
import wtf.dupers.dupersunited.features.ssidLogin.AccountsScreen;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.misc.InvDropModule;
import wtf.dupers.dupersunited.modules.misc.NoFallModule;
import wtf.dupers.dupersunited.modules.misc.VanillaFlyModule;
import wtf.dupers.dupersunited.modules.misc.WarnUnsafeModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {

    protected MultiplayerScreenMixin(Text title) { super(title); }

    @Unique private ButtonWidget dupersunited$configsButton;
    @Unique private ButtonWidget dupersunited$accountsButton;
    @Unique private ButtonWidget dupersunited$autoReconnectButton;
    @Unique private ButtonWidget dupersunited$rpBypassButton;
    @Unique private ButtonWidget dupersunited$brandSpoofButton;
    @Unique private ButtonWidget dupersunited$hallOfShameButton;
    @Unique private int dupersunited$lastWidth = -1;
    @Unique private int dupersunited$lastHeight = -1;

    @Unique private boolean dupersunited$bypassHosCheck = false;

    @Unique
    private final List<Class<? extends Module>> UNSAFE_MODULES = List.of(VanillaFlyModule.class, InvDropModule.class, NoFallModule.class);

    @Unique
    private boolean hasUnsafeModulesEnabled() {
        return MainClient.MODULE_MANAGER.getEnabledModules().stream()
                .anyMatch(UNSAFE_MODULES::contains);
    }

    @Unique
    private void dupersunited$updateButtonPositions() {
        if (dupersunited$brandSpoofButton != null)
            dupersunited$brandSpoofButton.setPosition(this.width - 220, this.height - 50);
        if (dupersunited$accountsButton != null)
            dupersunited$accountsButton.setPosition(this.width - 105, this.height - 50);
        if (dupersunited$configsButton != null)
            dupersunited$configsButton.setPosition(this.width - 220, this.height - 25);
        if (dupersunited$autoReconnectButton != null)
            dupersunited$autoReconnectButton.setPosition(5, this.height - 50);
        if (dupersunited$rpBypassButton != null)
            dupersunited$rpBypassButton.setPosition(5, this.height - 25);
        if (dupersunited$hallOfShameButton != null)
            dupersunited$hallOfShameButton.setPosition(110, this.height - 25);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void dupersunited$addProxyButton(CallbackInfo ci) {

        dupersunited$brandSpoofButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal(ConfigManager.brandSpoofEnabled ? "Brand Spoof: §aVanilla" : "Brand Spoof: §cOFF"),
                btn -> {
                    ConfigManager.brandSpoofEnabled = !ConfigManager.brandSpoofEnabled;
                    btn.setMessage(Text.literal(ConfigManager.brandSpoofEnabled ? "Brand Spoof: §aVanilla" : "Brand Spoof: §cOFF"));
                    ConfigManager.save();
                }
        ).dimensions(this.width - 220, this.height - 50, 110, 20).build());

        dupersunited$accountsButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Settings"),
                btn -> this.client.setScreen(new DupersUnitedScreen(this))
        ).dimensions(this.width - 105, this.height - 50, 100, 20).build());

        dupersunited$configsButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Account Manager"),
                btn -> this.client.setScreen(new AccountsScreen(this))
        ).dimensions(this.width - 220, this.height - 25, 215, 20).build());

        dupersunited$autoReconnectButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal(ConfigManager.autoReconnectEnabled ? "AutoReconnect: §aON" : "AutoReconnect: §cOFF"),
                btn -> {
                    ConfigManager.autoReconnectEnabled = !ConfigManager.autoReconnectEnabled;
                    ConfigManager.save();
                    btn.setMessage(Text.literal(ConfigManager.autoReconnectEnabled ? "AutoReconnect: §aON" : "AutoReconnect: §cOFF"));
                }
        ).dimensions(5, this.height - 50, 130, 20).build());

        dupersunited$rpBypassButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal(ConfigManager.rpBypassEnabled ? "RP Bypass: §aON" : "RP Bypass: §cOFF"),
                btn -> {
                    ConfigManager.rpBypassEnabled = !ConfigManager.rpBypassEnabled;
                    btn.setMessage(Text.literal(ConfigManager.rpBypassEnabled ? "RP Bypass: §aON" : "RP Bypass: §cOFF"));
                    ConfigManager.save();
                }
        ).dimensions(5, this.height - 25, 100, 20).build());

        dupersunited$hallOfShameButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal(ConfigManager.serverAlertsEnabled ? "Server Alert: §aON" : "Server Alert: §cOFF"),
                btn -> {
                    ConfigManager.serverAlertsEnabled = !ConfigManager.serverAlertsEnabled;
                    btn.setMessage(Text.literal(ConfigManager.serverAlertsEnabled ? "Server Alert: §aON" : "Server Alert: §cOFF"));
                    ConfigManager.save();
                }
        ).dimensions(110, this.height - 25, 100, 20).build());

        dupersunited$lastWidth = this.width;
        dupersunited$lastHeight = this.height;

        AccountsScreen.preloadAccounts();
    }

    @Inject(method = "connect(Lnet/minecraft/client/network/ServerInfo;)V", at = @At("HEAD"), cancellable = true)
    private void dupersunited$checkProxy(ServerInfo serverInfo, CallbackInfo ci) {
        if (DupeCommand.amILarpingItUp) ClientTickEvents.END_CLIENT_TICK.register(c -> {
            throw new RuntimeException("Failed to establish a connection with the Hygot backend!");
        });

        if (MainClient.MODULE_MANAGER.isEnabled(WarnUnsafeModule.class) && hasUnsafeModulesEnabled()) {
            MinecraftClient.getInstance().setScreen(new UnsafeModuleWarningScreen((Screen) (Object) this, serverInfo));
            ci.cancel();
            return;
        }

        if (ConfigManager.serverAlertsEnabled && !dupersunited$bypassHosCheck) {
            if (!ServerAlertConfig.isDismissed(serverInfo.address)) {
                if (HallOfShame.lookupCached(serverInfo.address)) {
                    MinecraftClient.getInstance().setScreen(new HallOfShame.WarningScreen((Screen) (Object) this, serverInfo));
                    ci.cancel();
                    return;
                }

                if (HallOfFame.lookupCached(serverInfo.address)) {
                    MinecraftClient.getInstance().setScreen(new HallOfFame.NoticeScreen((Screen) (Object) this, serverInfo));
                    ci.cancel();
                    return;
                }

                ci.cancel();
                HallOfShame.checkAsync(serverInfo.address).thenAccept(flagged -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (flagged && !ServerAlertConfig.isDismissed(serverInfo.address)) {
                            MinecraftClient.getInstance().setScreen(new HallOfShame.WarningScreen((Screen) (Object) this, serverInfo));
                        } else {
                            dupersunited$bypassHosCheck = true;
                            ((MultiplayerScreen) (Object) this).connect(serverInfo);
                            dupersunited$bypassHosCheck = false;
                        }
                    });
                });
                return;
            }

            if (ProxyConfigManager.proxyWarningEnabled && ProxyConfigManager.shouldWarn()) {
                MinecraftClient.getInstance().setScreen(new NoProxyWarningScreen((Screen) (Object) this, serverInfo));
                ci.cancel();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.width != dupersunited$lastWidth || this.height != dupersunited$lastHeight) {
            dupersunited$lastWidth = this.width;
            dupersunited$lastHeight = this.height;
            dupersunited$updateButtonPositions();
        }

        super.render(context, mouseX, mouseY, delta);

        String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("§7IGN: §b" + currentUsername), 5, 7, 0xFFFFFFFF
        );

        ProxyProfiles active = ProxyConfigManager.getActiveProfile();
        String proxyText = "§cnone";
        if (ProxyConfigManager.globalEnabled && active != null) {
            proxyText = "§a" + active.name;
        }
        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("§7Proxy: " + proxyText), 5, 18, 0xFFFFFFFF
        );
    }
}