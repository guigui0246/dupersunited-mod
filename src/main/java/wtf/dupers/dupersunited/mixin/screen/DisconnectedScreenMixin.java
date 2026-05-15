package wtf.dupers.dupersunited.mixin.screen;

import wtf.dupers.dupersunited.features.AutoReconnect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends net.minecraft.client.gui.screen.Screen {

    protected DisconnectedScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void dupersunited$startReconnect(CallbackInfo ci) {
        AutoReconnect.startCountdown();

        if (AutoReconnect.isCountingDown()) {
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            ButtonWidget btn = ButtonWidget.builder(
                            Text.literal("Reconnecting in " + AutoReconnect.getSecondsRemaining() + "s"),
                            b -> {
                                AutoReconnect.cancel();
                                b.setMessage(Text.literal("§cReconnect Cancelled"));
                                b.active = false;
                            }
                    ).dimensions(screenWidth / 2 - 100, screenHeight / 2 + 55, 200, 20)
                    .tooltip(Tooltip.of(Text.literal("Click this button to cancel auto reconnect!")))
                    .build();

            AutoReconnect.setCancelButton(btn);
            this.addDrawableChild(btn);
        }
    }
}