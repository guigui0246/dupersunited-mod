package wtf.dupers.dupersunited.features;

import wtf.dupers.dupersunited.commands.MainCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SaveGuiManager {
    static MinecraftClient mc = MinecraftClient.getInstance();
    public static String guiName;
    public static Screen savedScreen = null;
    public static ScreenHandler savedScreenHandler = null;
    public static boolean deadGui;

    private SaveGuiManager() {}

    public static void saveAndCloseGui() {
        if (mc.player != null) {
            savedScreen = mc.currentScreen;
            savedScreenHandler = mc.player.currentScreenHandler;
            if (savedScreen == null) {
                MainCommand.sendMessage(Text.literal("No GUI found to save.").formatted(Formatting.RED), true);
                return;
            }
            deadGui = false;
            mc.setScreen(null);
            if (savedScreen instanceof HandledScreen<?> handled) {
                MainCommand.sendMessage(Text.literal("Saved ")
                    .append(handled.getTitle())
                    .append(" GUI."), true);
                guiName = handled.getTitle().getString();
            } else {
                String classSimpleName = savedScreen.getClass().getSimpleName();
                MainCommand.sendMessage("Saved " + classSimpleName + " GUI.", true);
                guiName = classSimpleName;
            }}
    }

    public static void saveGui() {
        if (mc.player != null && mc.currentScreen != null) {
            savedScreen = mc.currentScreen;
            savedScreenHandler = mc.player.currentScreenHandler;
            deadGui = false;
            if (savedScreen instanceof HandledScreen<?> handled) {
                MainCommand.sendMessage(Text.literal("Saved ")
                    .append(handled.getTitle())
                    .append(" GUI."), true);
                guiName = handled.getTitle().getString();
            } else {
                String classSimpleName = savedScreen.getClass().getSimpleName();
                MainCommand.sendMessage("Saved " + classSimpleName + " GUI.", true);
                guiName = classSimpleName;
            }
        } else {
            MainCommand.sendMessage(Text.literal("No GUI found to save.").formatted(Formatting.RED), true);
        }
    }

    public static void restoreGui() {
        if (savedScreen != null && savedScreenHandler != null && mc.player != null) {
            deadGui = false;
            mc.setScreen(savedScreen);
            mc.player.currentScreenHandler = savedScreenHandler;
            savedScreen = null;
            savedScreenHandler = null;
            MainCommand.sendMessage(Text.literal("Restored ")
                .append(Text.literal(guiName).formatted(Formatting.AQUA))
                .append(" GUI."), true);
        } else {
            MainCommand.sendMessage(Text.literal("No saved GUI.").formatted(Formatting.RED), true);
        }
    }
}
