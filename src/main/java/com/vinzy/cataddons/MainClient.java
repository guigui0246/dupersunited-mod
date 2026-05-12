package com.vinzy.cataddons;

import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.events.GuiEvent;
import com.vinzy.cataddons.events.TickEvent;
import com.vinzy.cataddons.events.WorldEvent;
import com.vinzy.cataddons.features.ClickSlotManager;
import com.vinzy.cataddons.features.ConfigManager;
import com.vinzy.cataddons.features.HudOverlay;
import com.vinzy.cataddons.features.auth.AuthManager;
import com.vinzy.cataddons.features.chatmacros.*;
import com.vinzy.cataddons.features.proxies.*;
import com.vinzy.cataddons.features.screens.mainmenu.*;
import com.vinzy.cataddons.features.ssidLogin.*;
import com.vinzy.cataddons.keybinds.*;
import com.vinzy.cataddons.modules.*;
import com.vinzy.cataddons.modules.exploit.*;
import com.vinzy.cataddons.modules.glitcha.*;
import com.vinzy.cataddons.modules.misc.*;
import com.vinzy.cataddons.modules.render.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.vinzy.cataddons.features.proxies.ProxyConfigManager.profiles;
import static com.vinzy.cataddons.features.ssidLogin.SessionManager.*;

public class MainClient implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("DupersUnited");
    public static ModuleManager MODULE_MANAGER;

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");

        //SSID Login
        originalSession = SessionManager.getSession();
        currentSession = originalSession;
        overrideSession = true;

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

        //enable by defualt
        MODULE_MANAGER.getModule(WatermarkModule.class).setEnabled(true);
        MODULE_MANAGER.getModule(GuiUtilsModule.class).setEnabled(true);
        MODULE_MANAGER.getModule(TpsCounterModule.class).setEnabled(true);
        MODULE_MANAGER.getModule(WarnUnsafeModule.class).setEnabled(true);

        //config
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FreecamModule freecam = MODULE_MANAGER.getModule(FreecamModule.class);
            if (freecam != null && freecam.isEnabled()) freecam.setEnabled(false);
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::save));

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
        ChatMacroManager.load();
        KeybindManager.registerTickHandler();

        //config
        ProxyConfigManager.load();
        Map<String, ProxyProfiles> seen = new LinkedHashMap<>();
        for (ProxyProfiles p : profiles) {
            seen.putIfAbsent(p.name.toLowerCase(Locale.ROOT), p);
            MainClient.LOGGER.info("Removed " + p.name + " as it's a duplicate proxy.");
        }
        profiles = new ArrayList<>(seen.values());
        AccountProxyLinks.load();
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

        ConfigManager.load();
        ChatMacroManager.load();

        //other
        ClickSlotManager.init();
        HudOverlay.init();
        AuthManager.init();
        CommandCat.register();

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
    }
}