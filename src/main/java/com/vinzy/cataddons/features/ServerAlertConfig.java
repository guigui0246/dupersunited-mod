package com.vinzy.cataddons.features;

import com.vinzy.cataddons.MainClient;
import com.google.gson.*;
import com.vinzy.cataddons.SharedVariables;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class ServerAlertConfig {

    private static final Path FILE = SharedVariables.DIRECTORY.resolve("whitelist-alerts.json");
    private static Set<String> dismissed = new HashSet<>();

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            String raw = Files.readString(FILE);
            JsonArray arr = JsonParser.parseString(raw).getAsJsonArray();
            for (JsonElement el : arr) {
                dismissed.add(el.getAsString().toLowerCase().trim());
            }
        } catch (Exception e) {
            MainClient.LOGGER.error("Failed to load server alert config", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonArray arr = new JsonArray();
            dismissed.forEach(arr::add);
            Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(arr));
        } catch (Exception e) {
            MainClient.LOGGER.error("Failed to save server alert config", e);
        }
    }

    public static boolean isDismissed(String address) {
        return dismissed.contains(normalise(address));
    }

    public static void dismiss(String address) {
        dismissed.add(normalise(address));
        save();
    }

    private static String normalise(String raw) {
        String s = raw.toLowerCase().trim();
        int colon = s.lastIndexOf(':');
        if (colon != -1 && s.indexOf(':') == colon) s = s.substring(0, colon);
        return s;
    }
}