package com.vinzy.cataddons.features.proxies;

import com.vinzy.cataddons.SharedVariables;
import com.vinzy.cataddons.features.AsyncConfigs;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AccountProxyLinks {
    private static final Path FILE = SharedVariables.DIRECTORY.resolve("accountsproxies.json");

    public static Map<String, String> links = new HashMap<>();
    public static Set<String> bypassAccounts = new ObjectOpenHashSet<>();
    public static Set<String> favoritedAccounts = new ObjectOpenHashSet<>();

    public static void save() {
        ConfigData data = new ConfigData(links, bypassAccounts, favoritedAccounts);
        AsyncConfigs.save(data, FILE, "account proxy links");
    }

    public static CompletableFuture<Void> load() {
        return AsyncConfigs.load(ConfigData.class, FILE, "account proxy links").thenAccept(data -> {
            links = data.links != null ? data.links : new HashMap<>();
            bypassAccounts = data.bypassAccounts != null ? data.bypassAccounts : new ObjectOpenHashSet<>();
            favoritedAccounts = data.favoritedAccounts != null ? data.favoritedAccounts : new ObjectOpenHashSet<>();
        });
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

    private record ConfigData(Map<String, String> links, Set<String> bypassAccounts, Set<String> favoritedAccounts) {}
}