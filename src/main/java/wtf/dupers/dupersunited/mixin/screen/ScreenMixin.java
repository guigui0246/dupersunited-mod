package wtf.dupers.dupersunited.mixin.screen;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.misc.PropagandaModule;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement {

//    @Inject(method = "init", at = @At("HEAD"))
//    private void onInit(CallbackInfo ci) {
//        GuiMacro.checkScreen((Screen)(Object)this);
//    }

    /* Propaganda Module */

    @Inject(method = "renderWithTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.AFTER))
    private void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        @Nullable PropagandaModule propagandaModule = MainClient.MODULE_MANAGER.getModule(PropagandaModule.class);
        if (propagandaModule != null && propagandaModule.isEnabled()) {
            context.createNewRootLayer();
            propagandaModule.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        @Nullable PropagandaModule propagandaModule = MainClient.MODULE_MANAGER.getModule(PropagandaModule.class);
        if (propagandaModule != null && propagandaModule.isEnabled()) {
            if (propagandaModule.mouseClicked(click, doubled)) {
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }
}