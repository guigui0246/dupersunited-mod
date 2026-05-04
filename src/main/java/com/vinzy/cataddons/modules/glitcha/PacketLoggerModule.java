package com.vinzy.cataddons.modules.glitcha;

import com.vinzy.cataddons.features.PacketLogger;
import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public class PacketLoggerModule extends Module {

    public final BooleanSetting logSigns = register(new BooleanSetting("Signs", true));
    public final BooleanSetting logGuiOpen = register(new BooleanSetting("GuiOpen", true));
    public final BooleanSetting logGuiClose = register(new BooleanSetting("GuiClose", true));
    public final BooleanSetting logGuiClick = register(new BooleanSetting("GuiClick", true));
    public final BooleanSetting logGuiUpdates = register(new BooleanSetting("GuiUpdates", true));

    public PacketLoggerModule() {
        super("PacketLogger", "Log inventory and GUI S2C and C2S packets (Can be very spammy!)", Category.glitcha);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}