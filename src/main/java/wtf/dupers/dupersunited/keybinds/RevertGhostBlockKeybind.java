package wtf.dupers.dupersunited.keybinds;

import wtf.dupers.dupersunited.api.keybind.Keybind;
import wtf.dupers.dupersunited.features.GhostBlock;
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