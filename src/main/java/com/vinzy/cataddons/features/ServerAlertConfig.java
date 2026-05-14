package com.vinzy.cataddons.features;

import com.google.gson.*;
import com.vinzy.cataddons.SharedVariables;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ServerAlertConfig {

    private static final Path FILE = SharedVariables.DIRECTORY.resolve("whitelist-alerts.json");
    private static final Set<String> dismissed = new ObjectOpenHashSet<>();

    public static void save() {
        JsonArray arr = new JsonArray();
        dismissed.forEach(arr::add);

        AsyncConfigs.save(arr, FILE, "server alert config");
    }

    public static CompletableFuture<Void> load() {
        return AsyncConfigs.load(JsonArray.class, FILE, "server alert config").thenAccept(arr -> {
            for (JsonElement el : arr) {
                dismissed.add(el.getAsString().toLowerCase().trim());
            }
        });
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