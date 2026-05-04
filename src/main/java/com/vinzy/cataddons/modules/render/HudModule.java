package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public class HudModule extends Module {
    public final BooleanSetting renderSetting = register(new BooleanSetting("Render", true));
    public final BooleanSetting miscSetting = register(new BooleanSetting("Misc", true));
    public final BooleanSetting glitchaSetting = register(new BooleanSetting("Glitcha", true));
 // public final BooleanSetting exploitSetting = register(new BooleanSetting("Exploit", true));

    public HudModule() {
        super("HUD", "Displays your enabled modules.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}