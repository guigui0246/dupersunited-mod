package wtf.dupers.dupersunited.mixin.screen;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.glitcha.GuiUtilsModule;
import wtf.dupers.dupersunited.modules.misc.PropagandaModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow protected int x;
    @Shadow protected int y;

//    @Inject(
//        method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
//        at = @At("HEAD")
//    )
//    private void dupersunited$onMouseClick(
//        Slot slot,
//        int slotId,
//        int button,
//        SlotActionType actionType,
//        CallbackInfo ci
//    ) {
//        if (!GuiMacro.isRecording) return;
//        if (slot == null) return;
//
//        GuiMacro.recordClick(slot.id, actionType, button);
//    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void dupersunited$keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (input.key() == 256) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) {
            for (Element child : client.currentScreen.children()) {
                if (child instanceof TextFieldWidget tf && tf.isFocused()) {
                    tf.keyPressed(input);
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }
            }
        }
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        GuiUtilsModule mod = MainClient.MODULE_MANAGER.getModule(GuiUtilsModule.class);
        if (mod == null) return;

        if (mod.isEnabled() && (boolean) mod.getSettingByName("SlotIds").getValue()) {
            mod.drawSlotId(context, slot);
            // CommandCat.sendMessage("drawing the slot id twin", true); it did draw
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void dupersunited$renderTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        @Nullable PropagandaModule propagandaModule = MainClient.MODULE_MANAGER.getModule(PropagandaModule.class);
        if (propagandaModule != null && propagandaModule.isEnabled()) {
            if (propagandaModule.renderTooltip(context, x, y)) {
                ci.cancel();
            }
        }
    }
}