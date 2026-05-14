package com.vinzy.cataddons.features.proxies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.SharedVariables;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AccountProxyLinks {
    private static final Path FILE = SharedVariables.DIRECTORY.resolve("accountsproxies.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Map<String, String> links = new HashMap<>();
    public static Set<String> bypassAccounts = new ObjectOpenHashSet<>();
    public static Set<String> favoritedAccounts = new ObjectOpenHashSet<>();

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                JsonObject root = new JsonObject();
                root.add("links", GSON.toJsonTree(links));
                root.add("bypassAccounts", GSON.toJsonTree(bypassAccounts));
                root.add("favoritedAccounts", GSON.toJsonTree(favoritedAccounts));

                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            MainClient.LOGGER.error("Failed to save account proxy links", e);
        }
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;

        try (Reader reader = Files.newBufferedReader(FILE)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root.has("links")) {
                Map<String, String> loaded = GSON.fromJson(root.get("links"), new TypeToken<Map<String, String>>(){}.getType());
                if (loaded != null) links = loaded;
            }
            if (root.has("bypassAccounts")) {
                Set<String> loaded = GSON.fromJson(root.get("bypassAccounts"), new TypeToken<Set<String>>(){}.getType());
                if (loaded != null) bypassAccounts = loaded;
            }
            if (root.has("favoritedAccounts")) {
                Set<String> loaded = GSON.fromJson(root.get("favoritedAccounts"), new TypeToken<Set<String>>(){}.getType());
                if (loaded != null) favoritedAccounts = loaded;
            }
        } catch (Exception e) {
            MainClient.LOGGER.error("Failed to load account proxy links", e);
        }
    }

    public static void link(String accountName, String proxyProfileName) {
        links.put(accountName, proxyProfileName);

        save();
    }

    public static void unlink(String accountName) {
        links.remove(accountName);
        save();
    }

    public static String getLinkedProxy(String accountName) {
        return links.get(accountName);
    }

    public static boolean hasLink(String accountName) {
        return links.containsKey(accountName);
    }

    public static boolean hasBypass(String accountName) {
        return bypassAccounts.contains(accountName);
    }

    public static void toggleBypass(String accountName) {
        if (bypassAccounts.contains(accountName)) {
            bypassAccounts.remove(accountName);
        } else {
            bypassAccounts.add(accountName);
        }
        save();
    }
    public static boolean isFavorite(String accountName) {
        return favoritedAccounts.contains(accountName);
    }

    public static void toggleFavorite(String accountName) {
        if (favoritedAccounts.contains(accountName)) {
            favoritedAccounts.remove(accountName);
        } else {
            favoritedAccounts.add(accountName);
        }
        save();
    }
}