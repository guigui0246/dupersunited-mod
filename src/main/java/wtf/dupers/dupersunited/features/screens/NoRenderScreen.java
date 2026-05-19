package wtf.dupers.dupersunited.features.screens;

import wtf.dupers.dupersunited.features.ConfigManager;
import wtf.dupers.dupersunited.modules.render.NoRenderModule;
import net.minecraft.text.Text;

public class NoRenderScreen extends EntitySelectionScreen {

    public NoRenderScreen() {
        super(Text.literal("No Render"), "No Render - Select Entities",
                NoRenderModule.selectedEntityIds, ConfigManager::save);
    }
}
