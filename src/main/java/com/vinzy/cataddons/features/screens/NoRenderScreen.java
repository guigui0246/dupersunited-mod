package com.vinzy.cataddons.features.screens;

import com.vinzy.cataddons.features.ConfigManager;
import com.vinzy.cataddons.modules.render.NoRenderModule;
import net.minecraft.text.Text;

public class NoRenderScreen extends EntitySelectionScreen {

    public NoRenderScreen() {
        super(Text.literal("No Render"), "No Render - Select Entities",
                NoRenderModule.selectedEntityIds, ConfigManager::save);
    }
}
