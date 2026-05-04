package com.vinzy.cataddons.mixin.screen;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<CreativeInventoryScreen.CreativeScreenHandler>{

    @Shadow
    private boolean ignoreTypedCharacter;

    @Shadow
    private static ItemGroup selectedTab;

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void cataddons$cacheServer(CharInput input, CallbackInfoReturnable<Boolean> cir) {
        if (this.ignoreTypedCharacter) cir.setReturnValue(false);
        if (selectedTab.getType() == ItemGroup.Type.SEARCH) return;
        super.charTyped(input);
    }
}   