package com.vinzy.cataddons.modules.render;

import com.vinzy.cataddons.features.screens.NoRenderScreen;
import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import com.vinzy.cataddons.modules.settings.ButtonSetting;
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
