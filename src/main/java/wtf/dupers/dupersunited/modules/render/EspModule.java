package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.features.screens.EntitySelectionScreen;
import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.ButtonSetting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class EspModule extends Module {

    public static final Set<EntityType<?>> selectedEntityIds = new ReferenceOpenHashSet<>();

    private final ButtonSetting selectEntities = register(
            new ButtonSetting("SelectEntities", this::openScreen)
    );

    public EspModule() {
        super("ESP", "Enables a glow on entities.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    public void openScreen() {
        MinecraftClient.getInstance().setScreen(
                new EntitySelectionScreen(Text.literal("ESP"), "ESP - Select Entities",
                        selectedEntityIds, ConfigManager::save));
    }
}
