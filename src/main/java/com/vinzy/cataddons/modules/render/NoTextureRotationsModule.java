package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
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