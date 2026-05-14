package com.vinzy.cataddons.features.chatmacros;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.SharedVariables;
import com.vinzy.cataddons.keybinds.Keybind;
import com.vinzy.cataddons.keybinds.KeybindManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

import static com.vinzy.cataddons.commands.CommandCat.sendMessage;

public class ChatMacroManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("DU/ChatMacros");

    private static final Path FILE = SharedVariables.DIRECTORY.resolve("chatmacros.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, ChatMacro> macros = new LinkedHashMap<>();

    private record ScheduledMessage(String text, long sendAt) {}

    private static final Queue<ScheduledMessage> queue = new LinkedList<>();

    public static void onTick() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        long now = System.currentTimeMillis();
        while (!queue.isEmpty() && queue.peek().sendAt() <= now) {
            String msg = queue.poll().text();
            if (msg.startsWith("/")) player.networkHandler.sendChatCommand(msg.substring(1));
            else
                player.networkHandler.sendChatMessage(msg);
        }
    }

    public static void addMacro(String name, List<MacroMessage> messages, int keyCode) {
        addMacro(name, messages, keyCode, false);
    }

    public static void addMacro(String name, List<MacroMessage> messages, int keyCode, boolean silent) {
        ChatMacro macro = new ChatMacro(name, messages, keyCode);
        macros.put(name.toLowerCase(Locale.ROOT), macro);
        registerKeybind(macro);
        save();
        if (!silent) {
            sendMessage(Text.literal("Created macro ")
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append("."), true);
        }
    }

    public static void removeMacro(String name) {
        removeMacro(name, false);
    }

    public static void removeMacro(String name, boolean silent) {
        ChatMacro removed = macros.remove(name.toLowerCase(Locale.ROOT));
        if (removed == null) return;

        KeybindManager.unregisterKeybind(name.toLowerCase(Locale.ROOT));

        save();

        if (!silent) {
            sendMessage(Text.literal("ChatMacro ")
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(" is now deleted."), true);
        }
    }

    public static void editMessages(String name, List<MacroMessage> messages) {
        ChatMacro macro = macros.get(name.toLowerCase(Locale.ROOT));
        if (macro == null) return;

        macro.setMessages(messages);
        save();
    }

    public static void rebind(String name, int newKey) {
        ChatMacro macro = macros.get(name.toLowerCase(Locale.ROOT));
        if (macro == null) {
            sendMessage(Text.literal("Macro ").formatted(Formatting.RED)
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(" not found."), true);
            return;
        }
        macro.setKeyCode(newKey);
        registerKeybind(macro);
        save();
    }

    public static Map<String, ChatMacro> getMacros() { return Collections.unmodifiableMap(macros); }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(FILE)) {
                Type type = new TypeToken<List<ChatMacro>>() {}.getType();
                GSON.toJson(new ArrayList<>(macros.values()), type, writer);
            }
        } catch (IOException e) {
            sendMessage(Text.empty()
                    .append(Text.literal("An error has occurred while trying to save chatmacros! ").formatted(Formatting.RED))
                    .append(e.getMessage()), true);

            MainClient.LOGGER.error("Error saving chatmacros", e);
        }
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;

        try (Reader reader = Files.newBufferedReader(FILE)) {
            List<ChatMacro> loaded = GSON.fromJson(reader, new TypeToken<>(){});

            if (loaded == null) return;
            for (ChatMacro macro : loaded) {
                macros.put(macro.getName().toLowerCase(Locale.ROOT), macro);
                registerKeybind(macro);
            }
        } catch (Exception e) {
            LOGGER.error("An error has occurred while trying to load chatmacros!", e);
        }
    }

    private static void registerKeybind(ChatMacro macro) {
        KeybindManager.registerKeybind(new Keybind(macro.getName().toLowerCase(Locale.ROOT), macro.getKeyCode()) {
            @Override
            public void onPress() {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null || client.currentScreen != null) return;

                long t = System.currentTimeMillis();
                for (MacroMessage msg : macro.getMessages()) {
                    if (msg.getText().isEmpty()) continue;
                    if (msg.getDelayMs() <= 0) {
                        // hopefulyl fixes my issue?!
                        if (msg.getText().startsWith("/")) client.player.networkHandler.sendChatCommand(msg.getText().substring(1));
                        else
                            client.player.networkHandler.sendChatMessage(msg.getText());
                    } else {
                        // has delay, now we queue it >_<
                        t += msg.getDelayMs();
                        queue.add(new ScheduledMessage(msg.getText(), t));
                    }
                }
            }
        });
    }
}