package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
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