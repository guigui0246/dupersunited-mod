package wtf.dupers.dupersunited.modules.glitcha;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import org.lwjgl.glfw.GLFW;

public class TpsCounterModule extends Module {
    public TpsCounterModule() {
        super("TPSCounter","Displays an estimate of the servers TPS on your HUD.", Category.glitcha);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
