package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
import wtf.dupers.dupersunited.modules.settings.FloatSetting;
import org.lwjgl.glfw.GLFW;

public class HidePlayersModule extends Module {
    public final BooleanSetting hideAll = register(new BooleanSetting("HideAll", true));
    public final FloatSetting distance = register(new FloatSetting("Distance", 5f, 3f, 100f));

    public HidePlayersModule() {
        super("HidePlayers", "Hides other players.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }
}
