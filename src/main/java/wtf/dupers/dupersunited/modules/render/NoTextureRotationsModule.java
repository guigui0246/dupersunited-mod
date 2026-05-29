package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.api.module.Category;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.BindSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class NoTextureRotationsModule extends Module {

    public NoTextureRotationsModule() {
        super("NoTextureRotations", "Removes texture rotations based on position.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onEnable() {
        refreshTerrain();
    }

    @Override
    protected void onDisable() {
        refreshTerrain();
    }

    private static void refreshTerrain() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        client.worldRenderer.reload();
    }
}