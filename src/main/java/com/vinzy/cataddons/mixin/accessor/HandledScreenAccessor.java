package com.vinzy.cataddons.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x") int cataddons$getGuiX();
    @Accessor("y") int cataddons$getGuiY();
    @Accessor("backgroundWidth") int cataddons$getBackgroundWidth();
    @Accessor("backgroundHeight") int cataddons$getBackgroundHeight();
}