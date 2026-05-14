package com.vinzy.cataddons.features;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.SharedVariables;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author Crosby
 */
public class AsyncConfigs {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /// ------
    /// Saving
    /// ------

    public static <T> CompletableFuture<Void> save(T data, Path file, String id) {
        return save(data, data.getClass(), file, id);
    }

    public static <T> CompletableFuture<Void> save(T data, Type type, Path file, String id) {
        return CompletableFuture.runAsync(() -> saveSync(data, type, file, id), SharedVariables.IO_EXECUTOR);
    }

    public static <T> void saveSync(T data, Path file, String id) {
        saveSync(data, data.getClass(), file, id);
    }

    public static <T> void saveSync(T data, Type type, Path file, String id) {
        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(data, type, writer);
            }
        } catch (IOException e) {
            MainClient.LOGGER.error("Error saving {}", id, e);
        }
    }

    /// -------
    /// Loading
    /// -------

    public static <T> CompletableFuture<T> load(Class<T> type, Path file, String id) {
        return load(TypeToken.get(type), file, id);
    }

    public static <T> CompletableFuture<T> load(TypeToken<T> type, Path file, String id) {
        return CompletableFuture.supplyAsync(() -> {
                MainClient.LOGGER.info("[{}] Read file", id);
                try { return loadInternal(type, file); }
                catch (IOException e) { throw new CompletionException(e); }
            }, SharedVariables.IO_EXECUTOR)
            .whenComplete((res, e) -> {
                if (e != null) MainClient.LOGGER.error("Error loading {}", id, e);
                MainClient.LOGGER.info("[{}] Post-read hook", id);
            });
    }

    public static <T> @Nullable T loadSync(Class<T> type, Path file, String id) {
        return loadSync(TypeToken.get(type), file, id);
    }

    public static <T> @Nullable T loadSync(TypeToken<T> type, Path file, String id) {
        try {
            return loadInternal(type, file);
        } catch (IOException | JsonSyntaxException e) {
            MainClient.LOGGER.error("Error loading {}", id, e);
            return null;
        }
    }

    private static <T> T loadInternal(TypeToken<T> type, Path file) throws IOException {
        if (!Files.isRegularFile(file)) return null;

        try (Reader reader = Files.newBufferedReader(file)) {
            @Nullable T value = GSON.fromJson(reader, type);
            if (value == null) throw new IOException("File was empty.");
            return value;
        }
    }
}
