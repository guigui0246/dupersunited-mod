package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

public class FreeLookModule extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public final FloatSetting sensitivity = register(new FloatSetting("Sensitivity", 8f, 0f, 20f));

    public float cameraYaw;
    public float smoothYaw;

    public FreeLookModule() {
        super("FreeLook", "Allows you to orbit your camera around your player without moving your head.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) return;
        cameraYaw = mc.player.getYaw();
        smoothYaw = cameraYaw;
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override
    protected void onDisable() {
        mc.options.setPerspective(Perspective.FIRST_PERSON);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.options.getPerspective() != Perspective.THIRD_PERSON_BACK)
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    public boolean isPlayerMode() {
        return isEnabled() && mc.options.getPerspective() == Perspective.THIRD_PERSON_BACK;
    }
}