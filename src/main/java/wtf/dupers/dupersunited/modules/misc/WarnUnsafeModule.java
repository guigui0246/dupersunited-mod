package wtf.dupers.dupersunited.modules.misc;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import org.lwjgl.glfw.GLFW;

public class WarnUnsafeModule extends Module {
    public WarnUnsafeModule() {
        super("WarnUnsafeMods", "Warns you when joining a server if you have unsafe modules enabled.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
