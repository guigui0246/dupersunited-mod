package com.vinzy.cataddons.modules.misc;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import org.lwjgl.glfw.GLFW;

public class WarnUnsafeModule extends Module {
    public WarnUnsafeModule() {
        super("WarnUnsafeMods", "Warns you when joining a server if you have unsafe modules enabled.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
