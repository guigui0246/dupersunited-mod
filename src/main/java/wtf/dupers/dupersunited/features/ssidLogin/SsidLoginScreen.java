package wtf.dupers.dupersunited.features.ssidLogin;

import wtf.dupers.dupersunited.features.proxies.AccountProxyLinks;
import wtf.dupers.dupersunited.features.proxies.LinkProxyScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import static wtf.dupers.dupersunited.utils.ColorUtil.*;

public class SsidLoginScreen extends Screen {
    private TextFieldWidget sessionField;
    private ButtonWidget loginButton;
    private ButtonWidget restoreButton;
    private ButtonWidget bypassButton;
    private ButtonWidget linkProxyButton;
    private Text currentTitle;

    private final Screen parent;

    public SsidLoginScreen(Screen parent) {
        super(Text.literal("SSID Login"));
        this.currentTitle = Text.literal("Input Session ID").formatted(Formatting.LIGHT_PURPLE);
        this.parent = parent;
    }


    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.sessionField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100,
            centerY - 10,
            200,
            20,
            Text.literal("Session Input")
        );

        this.sessionField.setMaxLength(32767);
        this.sessionField.setText("");
        this.sessionField.setFocused(true);

        this.addSelectableChild(this.sessionField);

        this.loginButton = ButtonWidget.builder(
            Text.literal("Login"),
            button -> {
                String sessionInput = this.sessionField.getText().trim();

                if (!sessionInput.isEmpty()) {
                    try {
                        String[] sessionInfo = SessionAPI.getProfileInfo(sessionInput);

                        if (sessionInfo == null) {
                            this.currentTitle = Text.literal("Invalid Session ID").formatted(Formatting.RED);
                            return;
                        }

                        SessionManager.setSession(
                            SessionManager.createSession(
                                sessionInfo[0],
                                sessionInfo[1],
                                sessionInput
                            )
                        );

                        this.currentTitle = Text.literal("Logged in as: " + sessionInfo[0])
                            .formatted(Formatting.GREEN);

                        this.restoreButton.active = true;
                        updateProxyButtons(sessionInfo[0]);

                    } catch (RuntimeException e) {
                        this.currentTitle = Text.literal("Error occurred during login, try refreshing your tokens.")
                            .formatted(Formatting.RED);
                    }
                } else {
                    this.currentTitle = Text.literal("Session ID cannot be empty")
                        .formatted(Formatting.RED);
                }
            }
        ).dimensions(centerX - 100, centerY + 15, 97, 20).build();

        this.addDrawableChild(this.loginButton);

        this.restoreButton = ButtonWidget.builder(
            Text.literal("Restore"),
            button -> {
                SessionManager.restoreSession();
                this.currentTitle = Text.literal("Restored original session")
                    .formatted(Formatting.GREEN);

                this.loginButton.active = true;
                this.restoreButton.active = false;
                updateProxyButtons(SessionManager.getUsername());
            }
        ).dimensions(centerX + 3, centerY + 15, 97, 20).build();

        this.addDrawableChild(this.restoreButton);

        // bypass toggle button
        this.bypassButton = ButtonWidget.builder(
                Text.literal("Bypass Proxy: Off"),
                button -> {
                    String user = SessionManager.getUsername();
                    AccountProxyLinks.toggleBypass(user);
                    updateProxyButtons(user);
                }
            ).dimensions(centerX - 100, centerY + 40, 97, 20)
            .tooltip(Tooltip.of(Text.literal("Toggle bypassing the proxy warning for this account")))
            .build();
        this.addDrawableChild(this.bypassButton);

        // link temp proxy button
        this.linkProxyButton = ButtonWidget.builder(
                Text.literal("Link Proxy"),
                button -> {
                    String user = SessionManager.getUsername();
                    client.setScreen(new LinkProxyScreen(this, user));
                }
            ).dimensions(centerX + 3, centerY + 40, 97, 20)
            .tooltip(Tooltip.of(Text.literal("Link a proxy profile to the current session")))
            .build();
        this.addDrawableChild(this.linkProxyButton);

        ButtonWidget backButton = ButtonWidget.builder(
            Text.literal("Back"),
            button -> {
                assert this.client != null;
                this.client.setScreen(
                    parent
                );
            }
        ).dimensions(centerX - 100, this.height - 25, 200, 18).build();

        this.addDrawableChild(backButton);

        if (SessionManager.currentSession.equals(SessionManager.originalSession)) {
            this.restoreButton.active = false;
        }

        updateProxyButtons(SessionManager.getUsername());
    }

    private void updateProxyButtons(String username) {
        if (bypassButton == null || linkProxyButton == null) return;

        boolean hasBypass = AccountProxyLinks.hasBypass(username);
        boolean hasLink = AccountProxyLinks.hasLink(username);

        bypassButton.setMessage(Text.literal(hasBypass ? "§aBypass: On" : "§7Bypass: Off"));
        linkProxyButton.setMessage(Text.literal(hasLink ? "§aLinked Proxy" : "§7Link Proxy"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // base background
        context.fill(0, 0, this.width, this.height, DEEP_SAPPHIRE);

        // top header bar
        context.fill(0, 0, this.width, 32, MANTLE);
        context.fill(0, 32, this.width, 33, FADED_INDIGO);

        // bottom footer bar
        context.fill(0, this.height - 35, this.width, this.height, MANTLE);
        context.fill(0, this.height - 36, this.width, this.height - 35, FADED_INDIGO);

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("SSID Login Manager").formatted(Formatting.BOLD),
            this.width / 2,
            12,
            LAVENDER
        );

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.currentTitle,
            this.width / 2,
            this.height / 2 - 30,
            0xFFFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);

        this.sessionField.render(context, mouseX, mouseY, delta);

        if (sessionField.getText().isEmpty()) {
            context.drawTextWithShadow(textRenderer, Text.literal("Paste Session ID here..."), sessionField.getX() + 4, sessionField.getY() + 6, 0xAAFFFFFF);
        }
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.sessionField.keyPressed(input) || this.sessionField.isActive()) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (this.sessionField.charTyped(input)) {
            return true;
        }
        return super.charTyped(input);
    }
}