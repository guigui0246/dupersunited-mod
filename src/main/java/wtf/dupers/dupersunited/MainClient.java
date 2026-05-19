package wtf.dupers.dupersunited;

import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.events.GuiEvent;
import wtf.dupers.dupersunited.events.TickEvent;
import wtf.dupers.dupersunited.events.WorldEvent;
import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.features.HudOverlay;
import wtf.dupers.dupersunited.features.ServerAlertConfig;
import wtf.dupers.dupersunited.features.auth.AuthManager;
import wtf.dupers.dupersunited.features.chatmacros.ChatMacroManager;
import wtf.dupers.dupersunited.features.proxies.AccountProxyLinks;
import wtf.dupers.dupersunited.features.proxies.ProxyConfigManager;
import wtf.dupers.dupersunited.features.screens.mainmenu.WelcomeScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.alerts.HallOfFame;
import wtf.dupers.dupersunited.features.screens.mainmenu.alerts.HallOfShame;
import wtf.dupers.dupersunited.keybinds.*;
import wtf.dupers.dupersunited.modules.glitcha.*;
import wtf.dupers.dupersunited.modules.misc.*;
import wtf.dupers.dupersunited.modules.render.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.dupers.dupersunited.features.ssidLogin.SessionManager;
import wtf.dupers.dupersunited.modules.ModuleManager;
import wtf.dupers.dupersunited.modules.exploit.AnySignModule;
import wtf.dupers.dupersunited.modules.exploit.BookBotModule;

import java.util.concurrent.CompletableFuture;

import static wtf.dupers.dupersunited.features.ssidLogin.SessionManager.*;

public class MainClient implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("DupersUnited");
    public static ModuleManager MODULE_MANAGER;

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");

        //SSID Login
        originalSession = SessionManager.getSession();

        //modules
        MODULE_MANAGER = new ModuleManager();
        MODULE_MANAGER.register(new EspModule());
        MODULE_MANAGER.register(new FullBrightModule());
        MODULE_MANAGER.register(new AutoSprintModule());
        MODULE_MANAGER.register(new PacketLoggerModule());
        MODULE_MANAGER.register(new WatermarkModule());
        MODULE_MANAGER.register(new HidePlayersModule());
        MODULE_MANAGER.register(new AnySignModule());
        MODULE_MANAGER.register(new NickModule());
        MODULE_MANAGER.register(new GuiUtilsModule());
        MODULE_MANAGER.register(new TpsCounterModule());
        MODULE_MANAGER.register(new FreeLookModule());
        MODULE_MANAGER.register(new FreecamModule());
        MODULE_MANAGER.register(new WarnUnsafeModule());
        MODULE_MANAGER.register(new HudModule());
        MODULE_MANAGER.register(new BookBotModule());
        MODULE_MANAGER.register(new PropagandaModule());
        MODULE_MANAGER.register(new PacketDelayModule());
        MODULE_MANAGER.register(new PayAllSettingsModule());
        MODULE_MANAGER.register(new InvDropModule());
        MODULE_MANAGER.register(new BlockEspModule());
        MODULE_MANAGER.register(new NoRenderModule());
        MODULE_MANAGER.register(new NoTextureRotationsModule());
        MODULE_MANAGER.register(new BetterTabModule());
        MODULE_MANAGER.register(new ChatStackerModule());
        MODULE_MANAGER.register(new ClickSlotModule());
        MODULE_MANAGER.register(new VanillaFlyModule());
        MODULE_MANAGER.register(new NoFallModule());
        MODULE_MANAGER.register(new SpamModule());

        //enable by default
        MODULE_MANAGER.getModule(WatermarkModule.class).setEnabled(true);
        MODULE_MANAGER.getModule(GuiUtilsModule.class).setEnabled(true);
        MODULE_MANAGER.getModule(TpsCounterModule.class).setEnabled(true);
        MODULE_MANAGER.getModule(WarnUnsafeModule.class).setEnabled(true);

        //config
        CompletableFuture<Void> proxyConfigTask = CompletableFuture.allOf(
            ProxyConfigManager.load(),
            AccountProxyLinks.load()
        ).thenAccept(nil -> {
            // auto apply proxy linked to the launch account if it exists
            String launchUsername = SessionManager.getSession() != null ? SessionManager.getSession().getUsername() : null;
            if (launchUsername != null && AccountProxyLinks.hasLink(launchUsername) && !AccountProxyLinks.hasBypass(launchUsername)) {
                String linkedProxy = AccountProxyLinks.getLinkedProxy(launchUsername);
                ProxyConfigManager.activeProfileName = linkedProxy;
                ProxyConfigManager.globalEnabled = true;
                ProxyConfigManager.save();
                //LOGGER.info("Auto Applied Proxy Profile '{}' for launch account '{}'", linkedProxy, launchUsername); worked
            } else if (launchUsername != null && !AccountProxyLinks.hasLink(launchUsername)) {
                // no proxy linked to this account, make sure proxy is disabled haha oopsie
                ProxyConfigManager.globalEnabled = false;
                ProxyConfigManager.activeProfileName = "";
                ProxyConfigManager.save();
                //LOGGER.info("No proxy linked for launch account '{}', disabling proxy!", launchUsername); bamgangnbang
            }

            MainClient.LOGGER.info("[Session proxies] Composed");
        });

        CompletableFuture<Void> configLoadTask = CompletableFuture.allOf(
            proxyConfigTask,
            ConfigManager.load(),
            ServerAlertConfig.load(),
            ChatMacroManager.load()
        );

        HallOfShame.prefetch();
        HallOfFame.prefetch();

        //config
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FreecamModule freecam = MODULE_MANAGER.getModule(FreecamModule.class);
            if (freecam != null && freecam.isEnabled()) freecam.setEnabled(false);

            ConfigManager.saveBlocking();
        }));

        //events
        WorldEvent.register();
        GuiEvent.register();
        TickEvent.register();

        //keybinds
        ClickGuiKeybind.register();
        KeybindManager.registerKeybind(new RestoreGuiKeybind());
        KeybindManager.registerKeybind(new RevertGhostBlockKeybind());
        KeybindManager.registerKeybind(new SaveGuiKeybind());
        KeybindManager.registerKeybind(new PacketPauseKeybind());
        KeybindManager.registerKeybind(new GhostBlockKeybind());
        KeybindManager.registerKeybind(JoinServerInviteKeybind.INSTANCE);

        //other
        HudOverlay.init();
        AuthManager.init();
        MainCommand.register();

        // first launch shizz
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen && ConfigManager.firstLaunch) {
                ConfigManager.firstLaunch = false;
                ConfigManager.save();
                client.execute(() -> {
                    client.setScreen(new WelcomeScreen(screen));
                });
            }
        });

        // ensure configs loaded before finishing init
        try {
            configLoadTask.join();
        } catch (Exception ignored) {}
    }
}