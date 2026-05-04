package com.vinzy.cataddons.features.proxies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AccountProxyLinks {
    private static final File FOLDER = new File(MinecraftClient.getInstance().runDirectory, "DupersUnited");
    private static final File FILE = new File(FOLDER,"accountsproxies.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Map<String, String> links = new HashMap<>();
    public static Set<String> bypassAccounts = new ObjectOpenHashSet<>();

    public static void save() {
        if (!FOLDER.exists()) FOLDER.mkdirs();
        try (FileWriter writer = new FileWriter(FILE)) {
            JsonObject root = new JsonObject();
            root.add("links", GSON.toJsonTree(links));
            root.add("bypassAccounts", GSON.toJsonTree(bypassAccounts));
            GSON.toJson(root, writer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!FILE.exists()) return;
        try (FileReader reader = new FileReader(FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("links")) {
                Map<String, String> loaded = GSON.fromJson(root.get("links"), new TypeToken<Map<String, String>>(){}.getType());
                if (loaded != null) links = loaded;
            }
            if (root.has("bypassAccounts")) {
                Set<String> loaded = GSON.fromJson(root.get("bypassAccounts"), new TypeToken<Set<String>>(){}.getType());
                if (loaded != null) bypassAccounts = loaded;
            }
        } catch (Exception e) {
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
}