package com.vinzy.cataddons.features.proxies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ProxyConfigManager {
    private static final File FOLDER = new File(MinecraftClient.getInstance().runDirectory, "DupersUnited");
    private static final File FILE = new File(FOLDER,"proxies.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean globalEnabled = false;
    public static boolean proxyWarningEnabled = false;
    public static String activeProfileName = "";
    public static List<ProxyProfiles> profiles = new ArrayList<>();
    public static List<String> customAccountPaths = new ArrayList<>();

    public static void save() {
        if (!FOLDER.exists()) FOLDER.mkdirs();
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(new ConfigData(globalEnabled, proxyWarningEnabled, activeProfileName, profiles, customAccountPaths), writer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!FILE.exists()) return;
        try (FileReader reader = new FileReader(FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                globalEnabled = data.globalEnabled;
                proxyWarningEnabled = data.proxyWarningEnabled;
                activeProfileName = data.activeProfileName;
                profiles = data.profiles != null ? data.profiles : new ArrayList<>();
                customAccountPaths = data.customAccountPaths != null ? data.customAccountPaths : new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static ProxyProfiles getActiveProfile() {
        return profiles.stream().filter(p -> p.name.equals(activeProfileName)).findFirst().orElse(null);
    }

    public static String getWarningReason() {
        if (!globalEnabled)
            return "§7You currently have proxies fully §cdisabled§7!";
        if (getActiveProfile() == null)
            return "§7You have no proxy profile §cselected§7 for this account!";
        return "";
    }

    public static boolean shouldWarn() {
        return !globalEnabled || getActiveProfile() == null;
    }

    private record ConfigData(boolean globalEnabled, boolean proxyWarningEnabled, String activeProfileName, List<ProxyProfiles> profiles, List<String> customAccountPaths) {}
}