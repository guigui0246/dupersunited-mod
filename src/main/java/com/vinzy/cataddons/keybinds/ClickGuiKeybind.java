package com.vinzy.cataddons.keybinds;

import com.vinzy.cataddons.SharedVariables;
import com.vinzy.cataddons.features.screens.ClickGui;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClickGuiKeybind {

    public static KeyBinding keyBinding;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private ClickGuiKeybind() {}

    public static void register() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Click GUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                SharedVariables.CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed()) {
                handleClick();
            }
        });
    }

    public static void handleClick() {
        if (mc.player != null) {
            Screen parentScreen = mc.currentScreen;
            SharedVariables.screenToOpen = new ClickGui(parentScreen);
        }
    }
}