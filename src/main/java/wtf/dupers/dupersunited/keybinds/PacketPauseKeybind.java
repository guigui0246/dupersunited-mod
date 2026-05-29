package wtf.dupers.dupersunited.keybinds;

import wtf.dupers.dupersunited.api.keybind.Keybind;
import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.features.PacketPauseManager;
import wtf.dupers.dupersunited.modules.glitcha.PacketDelayModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class PacketPauseKeybind extends Keybind {

    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static long blinkStartTime = 0;

    public PacketPauseKeybind() {
        super("Delay Packets", GLFW.GLFW_KEY_F7);
    }

    @Override
    public int getKeyCode() {
        return PacketDelayModule.blinkBind.getValue();
    }

    @Override
    public void setKeyCode(int keyCode) {
        PacketDelayModule.blinkBind.setValue(keyCode);
    }

    public static long getBlinkStartTime() {
        return blinkStartTime;
    }

    @Override
    public void onPress() {
        handleToggle();
    }

    public static void handleToggle() {
        if (client.getNetworkHandler() == null) return;

        if (isShiftDown()) {
            handleCancel();
        } else {
            boolean wasPaused = PacketPauseManager.isPaused();
            int packetCount = PacketPauseManager.getPacketQueue().size();

            PacketPauseManager.toggle();

            if (client.player != null) {
                client.setScreen(client.currentScreen); //prolly better way to do this but wtv bro
                if (wasPaused) {
                    blinkStartTime = 0;
                    MainCommand.sendMessage(Text.literal("Sent ")
                            .append(Text.literal(Integer.toString(packetCount)).formatted(Formatting.AQUA))
                            .append(" packets."), true);
                    MainCommand.sendMessage(Text.literal("Packets are now ")
                            .append(Text.literal("resumed").formatted(Formatting.GREEN))
                            .append("."), true);
                } else {
                    MainCommand.sendMessage(Text.literal("Packets are now ")
                            .append(Text.literal("paused").formatted(Formatting.RED))
                            .append("."), true);
                    blinkStartTime = System.currentTimeMillis();
                }
            }
        }
    }

    private static boolean isShiftDown() {
        long window = client.getWindow().getHandle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    public static void handleCancel() {
        if (!PacketPauseManager.isPaused()) {
            if (client.player != null) {
                MainCommand.sendMessage("Cannot cancel packets because blink is not active.", true);
            }
            return;
        }
        int packetCount = PacketPauseManager.getPacketQueue().size();

        PacketPauseManager.clear();
        PacketPauseManager.toggle();

        blinkStartTime = 0;

        if (client.player != null) {
            MainCommand.sendMessage(Text.literal("Blink cancelled, cleared ")
                    .append(Text.literal(Integer.toString(packetCount)).formatted(Formatting.AQUA))
                    .append(" packets."), true);
        }
    }
}