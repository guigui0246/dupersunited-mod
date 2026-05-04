package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import com.vinzy.cataddons.modules.settings.FloatSetting;
import org.lwjgl.glfw.GLFW;


public class HidePlayersModule extends Module {
    public final BooleanSetting hideAll = register(new BooleanSetting("HideAll", true));
    public final FloatSetting distance = register(new FloatSetting("Distance", 5f, 3f, 100f));

    public HidePlayersModule() {
        super("HidePlayers", "Hides other players.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
