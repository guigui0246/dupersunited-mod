package com.vinzy.cataddons.keybinds;

import com.vinzy.cataddons.features.GhostBlock;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class RevertGhostBlockKeybind extends Keybind {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public RevertGhostBlockKeybind() {
        super("Revert GB", GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onPress() {
        if (mc.player != null) {
            GhostBlock.restoreGhosts();
        }
    }
}