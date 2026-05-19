package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
import wtf.dupers.dupersunited.modules.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class FullBrightModule extends Module {

    private MinecraftClient mc = MinecraftClient.getInstance();

    public final ModeSetting mode = register(new ModeSetting("Mode", "Ambient", "Ambient", "Potion", "Gamma"));
    public final BooleanSetting noEffect = register(new BooleanSetting("NoEffect", false));

    public static final float gamma = 1600.0f;
    public static final float ambient = 1.0f;


    public FullBrightModule() {
        super("Fullbright","Makes the bright full.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            if (mode.getValue().equals("Potion")) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 840));
            } else if (noEffect.getValue()) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
    }
}
