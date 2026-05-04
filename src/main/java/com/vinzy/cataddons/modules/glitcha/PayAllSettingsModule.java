package com.vinzy.cataddons.modules.glitcha;

import com.vinzy.cataddons.features.PayAllManager;
import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import com.vinzy.cataddons.modules.settings.IntSetting;
import com.vinzy.cataddons.modules.settings.StringSetting;
import org.lwjgl.glfw.GLFW;

public class PayAllSettingsModule extends Module {
    public final StringSetting commandSetting = register(new StringSetting("Command", "/pay <p> <a>"));
    public final StringSetting payAmount = register(new StringSetting("Money", "", 256));
    public final BooleanSetting autoDivideAmt = register(new BooleanSetting("Auto Divide", true));
    public final BooleanSetting doubleSend = register(new BooleanSetting("Double Send", false));
    public final IntSetting delayBetweenPay = register(new IntSetting("Delay", 20, 1, 100));

    public PayAllSettingsModule() {
        super("PayAll", "Pays everyone, right click this module to customize the payAll subcommand, <p> for player and <a> for amount.", Category.glitcha);
        this.register(new BindSetting("Pay Everyone", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    public void toggle() {
        if (PayAllManager.isRunning()) {
            PayAllManager.stopPayAll();
        } else {
            PayAllManager.startPayAll();
        }
    }
}