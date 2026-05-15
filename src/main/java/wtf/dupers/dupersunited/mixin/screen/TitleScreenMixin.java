package wtf.dupers.dupersunited.mixin.screen;

import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.features.screens.DupersUnitedScreen;
import wtf.dupers.dupersunited.features.screens.mainmenu.*;
import wtf.dupers.dupersunited.features.ssidLogin.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.dupers.dupersunited.features.ssidLogin.SessionAPI;
import wtf.dupers.dupersunited.features.ssidLogin.SessionManager;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private SplashTextRenderer splashText;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.splashText = new SplashTextRenderer(SharedVariables.randomQuote());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addButton(CallbackInfo ci) {

        int x = this.width;

        /*
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("SSID Login"),
                        button -> {
                            assert this.client != null;
                            this.client.setScreen(new LoginScreen(MinecraftClient.getInstance().currentScreen));
                        }
                ).dimensions(centerX - 100, y, 200, 20).build()
        );
        */

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("DupersUnited"),
                        button -> {
                            assert this.client != null;
                            this.client.setScreen(new DupersUnitedScreen(MinecraftClient.getInstance().currentScreen));
                        }
                ).dimensions(x - 105, 5, 100, 20).build()
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        super.render(context, mouseX, mouseY, delta);

        String username = SessionManager.getUsername();

        if (SessionManager.isSessionValid == null && !SessionManager.hasValidationStarted) {
            SessionManager.hasValidationStarted = true;

            new Thread(() -> {
                    SessionManager.isSessionValid = SessionAPI.validateSession(this.client.getSession().getAccessToken());
                    SessionManager.hasValidationStarted = false;
                }, "SessionValidationThread"
            ).start();
        }

        Text status;

        if (SessionManager.isSessionValid == null) {
            status = Text.literal("[... Validating]")
                    .formatted(Formatting.GRAY);
        } else if (SessionManager.isSessionValid) {
            status = Text.literal("[Valid]")
                    .formatted(Formatting.GREEN);
        } else {
            status = Text.literal("[Invalid]")
                    .formatted(Formatting.RED);
        }

        Text playerDisplay = Text.literal("User: ")
                .append(Text.literal(username).formatted(Formatting.AQUA))
                .append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
                .append(status);

        context.drawText(this.textRenderer, playerDisplay, 5, 5, -1, true);
    }
}
