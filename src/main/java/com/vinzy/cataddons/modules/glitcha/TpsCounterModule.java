package com.vinzy.cataddons.modules.glitcha;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import org.lwjgl.glfw.GLFW;

public class TpsCounterModule extends Module {
    public TpsCounterModule() {
        super("TPSCounter","Displays an estimate of the servers TPS on your HUD.", Category.glitcha);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
