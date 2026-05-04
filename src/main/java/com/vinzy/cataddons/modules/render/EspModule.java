package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.features.ConfigManager;
import com.vinzy.cataddons.features.screens.EntitySelectionScreen;
import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.ButtonSetting;
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
