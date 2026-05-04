package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.StringSetting;
import org.lwjgl.glfw.GLFW;

public class WatermarkModule extends Module {
    public final StringSetting watermarkText = register(new StringSetting("Text", "§bDupersUnited §7(discord.gg/dupes)"));

    public WatermarkModule(){
        super("Watermark", "Adds a watermark to your HUD.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
