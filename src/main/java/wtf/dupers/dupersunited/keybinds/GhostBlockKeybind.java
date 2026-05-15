package wtf.dupers.dupersunited.keybinds;

import wtf.dupers.dupersunited.features.GhostBlock;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class GhostBlockKeybind extends Keybind {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public GhostBlockKeybind() {
        super("Ghost Blocks", GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onPress() {
        if (mc.player != null) {
            GhostBlock.deleteBlock();
        }
    }
}