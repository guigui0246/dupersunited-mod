package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import com.vinzy.cataddons.modules.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class NickModule extends Module {
    private final StringSetting nickname = register(new StringSetting("Name", "Duper"));
    public final BooleanSetting censor = register(new BooleanSetting("Censor", false));
    public final BooleanSetting onlyIngame = register(new BooleanSetting("OnlyIngame", false));

    public String username;

    public NickModule() {
        super("NameChanger","Changes your displayed name", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onEnable() {
        username = MinecraftClient.getInstance().getSession().getUsername();
    }

    public String replaceName(String string) {
        if (string != null && isEnabled()) {
            if (censor.getValue()) {
                if (username.length() <= 2) return string;
                String censored = username.substring(0, 2) + "*".repeat(username.length() - 2);
                return string.replace(username, censored);
            }
            String nick = nickname.getValue().replace("&", "§");
            return string.replace(username, nick);
        }

        return string;
    }

}
