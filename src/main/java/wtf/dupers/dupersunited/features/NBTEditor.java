package wtf.dupers.dupersunited.features;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.mixin.accessor.EditBoxWidgetAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class NBTEditor extends Screen {
    private static final int TOPTEXT = 0xFF82D1E3; // light aqua
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BOX_WIDTH = 360;
    private static final int BOX_HEIGHT = 210;

    private final ItemStack stack;
    private EditBoxWidget nbtInput;

    public NBTEditor(ItemStack stack) {
        super(Text.literal("NBT Editor"));
        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();

        ClientPlayNetworkHandler networkHandler = getNetwork();
        if (networkHandler == null) return;

        try {
            var lookup = networkHandler.getRegistryManager();
            var nbt = (NbtCompound) ComponentMap.CODEC.encodeStart(
                    lookup.getOps(NbtOps.INSTANCE),
                    stack.getComponents()
            ).getOrThrow();

            initializeWidgets(nbt);
        } catch (Exception e) {
            MainCommand.sendMessage("Failed to load NBT: " + e.getMessage(), true);
            MainClient.LOGGER.error("Failed to load NBT", e);
            this.close();
        }
    }

    private void initializeWidgets(NbtCompound nbt) {
        int x = this.width / 2 - (BOX_WIDTH / 2);
        int y = this.height / 2 - (BOX_HEIGHT / 2);

        this.nbtInput = EditBoxWidgetAccessor.dupersunited$create(
                this.textRenderer,
                x,
                y,
                BOX_WIDTH,
                BOX_HEIGHT,
                Text.literal("NBT"),
                Text.empty(),
                WHITE,
                true,
                WHITE,
                true,
                false
        );
        this.nbtInput.setMaxLength(32767);

        this.nbtInput.setText(prettyPrintNBT(nbt.toString().replace("§", "&")));
        this.addDrawableChild(this.nbtInput);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§aSave Changes"), b -> saveNBT())
                .dimensions(this.width / 2 - 100, y + BOX_HEIGHT + 10, 200, 20)
                .build());

        this.setInitialFocus(this.nbtInput);
    }

    private void saveNBT() {
        ClientPlayNetworkHandler networkHandler = getNetwork();
        if (networkHandler == null) return;

        try {
            String rawText = this.nbtInput.getText().replace("&", "§");
            NbtCompound newNbt = StringNbtReader.readCompound(rawText.isBlank() ? "{}" : rawText);

            var ops = networkHandler.getRegistryManager().getOps(NbtOps.INSTANCE);
            ComponentMap map = ComponentMap.CODEC.parse(ops, newNbt)
                    .getOrThrow(msg -> new RuntimeException("Invalid NBT: " + msg));

            stack.applyComponentsFrom(map);
            MainCommand.sendMessage("NBT Updated.", true);
            this.close();
        } catch (Exception e) {
            MainCommand.sendMessage(Text.empty()
                .append(Text.literal("Failed to save! ").formatted(Formatting.RED))
                .append(e.getMessage()), true);

            MainClient.LOGGER.error("Failed to save NBT", e);
        }
    }

    private ClientPlayNetworkHandler getNetwork() {
        if (this.client == null) return null;
        return this.client.getNetworkHandler();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "NBT EDITOR", this.width / 2, 20, TOPTEXT);
    }

    private String prettyPrintNBT(String snbt) {
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;
        for (int i = 0; i < snbt.length(); i++) {
            char c = snbt.charAt(i);
            if (c == '"' && (i == 0 || snbt.charAt(i - 1) != '\\')) inQuotes = !inQuotes;
            if (!inQuotes) {
                if (c == '{' || c == '[') {
                    sb.append(c).append("\n").append("  ".repeat(++indent));
                    continue;
                }
                if (c == '}' || c == ']') {
                    indent = Math.max(0, indent - 1);
                    sb.append("\n").append("  ".repeat(indent)).append(c);
                    continue;
                }
                if (c == ',') {
                    sb.append(c).append("\n").append("  ".repeat(indent));
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return (this.nbtInput != null && this.nbtInput.keyPressed(input)) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return (this.nbtInput != null && this.nbtInput.charTyped(input)) || super.charTyped(input);
    }
}