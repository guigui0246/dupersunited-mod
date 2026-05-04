package com.vinzy.cataddons.keybinds;

import com.vinzy.cataddons.features.SaveGuiManager;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class RestoreGuiKeybind extends Keybind {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public RestoreGuiKeybind() {
        super("Restore GUI", GLFW.GLFW_KEY_V);
    }

    @Override
    public void onPress() {
        if (mc.player != null) {
            SaveGuiManager.restoreGui();
        }
    }
}