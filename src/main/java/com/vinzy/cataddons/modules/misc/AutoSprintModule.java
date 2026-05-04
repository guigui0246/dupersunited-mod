package com.vinzy.cataddons.modules.misc;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class AutoSprintModule extends Module {

    MinecraftClient mc = MinecraftClient.getInstance();
    private boolean wasSprinting = false;

    private final BooleanSetting waterCheck = register(new BooleanSetting("WaterCheck", true));

    public AutoSprintModule() {
        super("AutoSprint", "Automatically enables sprint.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onDisable() {
        wasSprinting = false;
    }

    @Override
    public void onTick() {
        if (isEnabled()) {
            if (mc.player != null) {
                if (waterCheck.getValue() && mc.player.isTouchingWater()) {
                    if (wasSprinting) {
                        mc.options.sprintKey.setPressed(false);
                        wasSprinting = false;
                    }
                    return;
                }
                mc.options.sprintKey.setPressed(true);
                wasSprinting = true;
            }
        }
    }
}