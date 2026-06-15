package wtf.dupers.dupersunited.modules.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.features.screens.EntitySelectionScreen;
import wtf.dupers.dupersunited.api.module.Category;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.BindSetting;
import wtf.dupers.dupersunited.api.module.settings.ButtonSetting;
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

    @Override
    public JsonElement writeJson() {
        JsonObject object = (JsonObject) super.writeJson();
        JsonArray noRenderEntities = new JsonArray();
        for (EntityType<?> type : selectedEntityIds) noRenderEntities.add(Registries.ENTITY_TYPE.getId(type).toString());
        object.add("selected-entity-ids", noRenderEntities);
        return object;
    }

    @Override
    public void readJson(JsonElement element) {
        super.readJson(element);
        if (element instanceof JsonObject object && object.has("selected-entity-ids")) {
            selectedEntityIds.clear();
            for (JsonElement el : object.getAsJsonArray("selected-entity-ids")) {
                Registries.ENTITY_TYPE.getEntry(Identifier.tryParse(el.getAsString())).ifPresent(entry -> selectedEntityIds.add(entry.value()));
            }
        }
    }
}
