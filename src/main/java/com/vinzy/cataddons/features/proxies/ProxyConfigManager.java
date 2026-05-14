package com.vinzy.cataddons.features.proxies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.SharedVariables;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProxyConfigManager {
    private static final Path FILE = SharedVariables.DIRECTORY.resolve("proxies.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean globalEnabled = false;
    public static boolean proxyWarningEnabled = false;
    public static String activeProfileName = "";
    public static List<ProxyProfiles> profiles = new ArrayList<>();
    public static List<String> customAccountPaths = new ArrayList<>();

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(new ConfigData(globalEnabled, proxyWarningEnabled, activeProfileName, profiles, customAccountPaths), writer);
            }
        } catch (IOException e) {
            MainClient.LOGGER.error("Error saving proxy configs", e);
        }
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;

        try (Reader reader = Files.newBufferedReader(FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                globalEnabled = data.globalEnabled;
                proxyWarningEnabled = data.proxyWarningEnabled;
                activeProfileName = data.activeProfileName;
                profiles = data.profiles != null ? data.profiles : new ArrayList<>();
                customAccountPaths = data.customAccountPaths != null ? data.customAccountPaths : new ArrayList<>();
            }
        } catch (IOException e) {
            MainClient.LOGGER.error("Error loading proxy configs", e);
        }
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