package com.vinzy.cataddons.features;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vinzy.cataddons.features.screens.ClickGui;

public class ModMenuIntegration implements ModMenuApi{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ClickGui(parent);
    }
}
