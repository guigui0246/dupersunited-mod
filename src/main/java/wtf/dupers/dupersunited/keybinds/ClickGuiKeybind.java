package wtf.dupers.dupersunited.keybinds;

import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.features.screens.ClickGui;
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
    }

    public static void onTick() {
        if (keyBinding.wasPressed() && mc.player != null) {
            Screen parentScreen = mc.currentScreen;
            SharedVariables.screenToOpen = new ClickGui(parentScreen);
        }
    }
}