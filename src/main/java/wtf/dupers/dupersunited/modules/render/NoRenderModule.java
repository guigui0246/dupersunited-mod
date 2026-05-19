package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.features.screens.NoRenderScreen;
import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
import wtf.dupers.dupersunited.modules.settings.ButtonSetting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class NoRenderModule extends Module {

    public static final Set<EntityType<?>> selectedEntityIds = new ReferenceOpenHashSet<>();

    public final BooleanSetting particles = register(new BooleanSetting("Particles", false));

    public final BooleanSetting plainObfuscatedText = register(new BooleanSetting("Glyphs", false));

    private final ButtonSetting selectEntities = register(
            new ButtonSetting("SelectEntities", this::openScreen)
    );

    public NoRenderModule() {
        super("NoRender", "Hides entity, particles and glyphs.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    public void openScreen() {
        MinecraftClient.getInstance().setScreen(new NoRenderScreen());
    }
}
