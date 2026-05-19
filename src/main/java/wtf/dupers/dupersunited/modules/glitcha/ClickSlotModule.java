package wtf.dupers.dupersunited.modules.glitcha;

import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
import wtf.dupers.dupersunited.modules.settings.StringSetting;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ClickSlotModule extends Module {

    private final StringSetting slot = new StringSetting("Slot", "0");
    private final StringSetting count = new StringSetting("Count", "1");
    private final StringSetting delayMs = new StringSetting("Delay", "50");
    private final BooleanSetting loop = new BooleanSetting("Loop", false);

    private int  clicksRemaining = 0;
    private long nextClickAt = 0L;

    public ClickSlotModule() {
        super("ClickSlot", "Sends left-click pickup packets to a slot a certain amount of times.", Category.glitcha);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
        this.register(slot);
        this.register(count);
        this.register(delayMs);
        this.register(loop);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    @Override
    public void onEnable() {
        resetClicks();

        MainCommand.sendMessage(Text.literal("Clicking slot ")
                .append(Text.literal(String.valueOf(getParsedSlot())).formatted(Formatting.GREEN))
                .append(" (x")
                .append(Text.literal(String.valueOf(getParsedCount())).formatted(Formatting.AQUA))
                .append(")"), true);
    }

    private void resetClicks() {
        clicksRemaining = getParsedCount();
        nextClickAt = System.currentTimeMillis();
    }

    // failsafes in case user is a dumbass and tries to put text into the click amount & delay
    private int getParsedSlot() {
        try {
            int val = Integer.parseInt(slot.getValue().trim());
            return Math.max(0, Math.min(val, 90));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int getParsedCount() {
        try {
            return Integer.parseInt(count.getValue().trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int getParsedDelay() {
        try {
            return Integer.parseInt(delayMs.getValue().trim());
        } catch (NumberFormatException e) {
            return 50;
        }
    }

    @Override
    public void onDisable() {
        clicksRemaining = 0;
        MainCommand.sendMessage("Finished clicking.", true);
    }

    private void onTick(MinecraftClient client) {
        if (!isEnabled()) return;

        if (clicksRemaining <= 0) {
            if (loop.getValue()) {
                resetClicks();
            } else {
                setEnabled(false);
                return;
            }
        }

        if (client.getNetworkHandler() == null) {
            setEnabled(false);
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        long now = System.currentTimeMillis();
        if (now < nextClickAt) return;

        short targetSlot = (short) getParsedSlot();

        ScreenHandler handler = (player.currentScreenHandler != null)
                ? player.currentScreenHandler
                : player.playerScreenHandler;

        if (targetSlot < 0 || targetSlot >= handler.slots.size()) {
            setEnabled(false);
            return;
        }

        ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                handler.syncId,
                handler.getRevision(),
                targetSlot,
                (byte) 0,
                SlotActionType.PICKUP,
                new Int2ObjectArrayMap<>(),
                ItemStackHash.EMPTY
        );

        client.getNetworkHandler().sendPacket(packet);

        clicksRemaining--;
        nextClickAt = now + Math.max(0, getParsedDelay());
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }
}