package wtf.dupers.dupersunited.mixin.glitcha;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.dupers.dupersunited.SharedVariables;

@Mixin(KeyBinding.Category.class)
public class KeyBindingCategoryMixin {
    // hardcode string to avoid having translation entries
    @Inject(method = "getLabel", at = @At("HEAD"), cancellable = true)
    private void replaceLabel(CallbackInfoReturnable<Text> cir) {
        if ((Object) this == SharedVariables.CATEGORY) {
            cir.setReturnValue(Text.literal("DupersUnited"));
        }
    }
}
