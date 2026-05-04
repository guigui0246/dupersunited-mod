package com.vinzy.cataddons.modules.misc;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class VanillaFlyModule extends Module {
    private final FloatSetting speed = register(new FloatSetting("Speed", 1.0f, 0.1f, 10.0f));

    public VanillaFlyModule() {
        super("VanillaFly", "Allows you to fly.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue() / 20f);
        mc.player.sendAbilitiesUpdate();
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().setFlySpeed(0.05f);
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue() / 20f);
        mc.player.sendAbilitiesUpdate();
    }
}
