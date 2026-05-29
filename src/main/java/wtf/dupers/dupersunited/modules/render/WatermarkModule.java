package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.api.module.Category;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.BindSetting;
import wtf.dupers.dupersunited.api.module.settings.StringSetting;
import org.lwjgl.glfw.GLFW;

public class WatermarkModule extends Module {
    public final StringSetting watermarkText = register(new StringSetting("Text", "§bDupersUnited §7(discord.gg/dupes)"));

    public WatermarkModule(){
        super("Watermark", "Adds a watermark to your HUD.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));

        //enable by default
        this.setEnabled(true);
    }
}
