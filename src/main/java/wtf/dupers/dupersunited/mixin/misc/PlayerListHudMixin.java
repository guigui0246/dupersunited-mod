package wtf.dupers.dupersunited.mixin.misc;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.misc.BetterTabModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(value = PlayerListHud.class, priority = 900)
public abstract class PlayerListHudMixin {
    @Shadow
    protected abstract List<PlayerListEntry> collectPlayerEntries();

    @WrapOperation(method = "collectPlayerEntries", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;"))
    private <T extends PlayerListEntry> Stream<T> replaceLimit(Stream<T> instance, long l, Operation<Stream<T>> original) {
        BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
        return original.call(instance, module.isEnabled() ? (long) module.tabSize.getValue() : l);
    }

    @ModifyReturnValue(method = "collectPlayerEntries", at = @At("RETURN"))
    private List<PlayerListEntry> applyScrollOffset(List<PlayerListEntry> original) {
        BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
        if (!module.isEnabled() || original.isEmpty()) return original;

        int max = module.tabSize.getValue();
        int total = original.size();

        module.scrollOffset = MathHelper.clamp(module.scrollOffset, 0, Math.max(0, total - 1));

        int start = module.scrollOffset;
        int end = Math.min(start + max, total);

        return original.subList(start, end);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> info) {
        BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
        if (module.isEnabled()) info.setReturnValue(module.getPlayerName(entry));
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), index = 0)
    private int modifyWidth(int width) {
        BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
        return module.isEnabled() && module.showPing.getValue() ? width + 30 : width;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", shift = At.Shift.BEFORE))
    private void modifyHeight(CallbackInfo ci, @Local(ordinal = 5) LocalIntRef o, @Local(ordinal = 6) LocalIntRef p) {
        BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
        if (!module.isEnabled()) return;

        int total = this.collectPlayerEntries().size();
        int rows = total;
        int cols = 1;
        while (rows > module.columnHeight.getValue()) {
            rows = (total + ++cols - 1) / cols;
        }

        o.set(rows);
        p.set(cols);
    }

    @Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
    private void onRenderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
        BetterTabModule module = MainClient.MODULE_MANAGER.getModule(BetterTabModule.class);
        if (!module.isEnabled()) return;

        if (!module.showPingIcon.getValue()) ci.cancel();

        if (module.showPing.getValue()) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int latency = MathHelper.clamp(entry.getLatency(), 0, 9999);
            int color = latency < 150 ? 0xFF00E970 :
                    latency < 300 ? 0xFFE7D020 : 0xFFD74238;
            String text = latency + "ms";
            context.drawTextWithShadow(textRenderer, text, x + width - textRenderer.getWidth(text), y, color);
            ci.cancel();
        }
    }
}