package wtf.dupers.dupersunited.keybinds;

import wtf.dupers.dupersunited.features.SaveGuiManager;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class SaveGuiKeybind extends Keybind {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public SaveGuiKeybind() {
        super("Save GUI", GLFW.GLFW_KEY_F6);
    }

    @Override
    public void onPress() {
        if (mc.player != null) {
            SaveGuiManager.saveAndCloseGui();
        }
    }
}