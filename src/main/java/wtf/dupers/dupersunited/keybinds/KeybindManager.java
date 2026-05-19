package wtf.dupers.dupersunited.keybinds;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.features.screens.*;
import wtf.dupers.dupersunited.features.screens.macroscreen.*;
import wtf.dupers.dupersunited.features.screens.mainmenu.KeybindScreen;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.mixin.accessor.KeyBindingAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class KeybindManager {

    private static final Map<KeyBinding, KeybindAction> keybinds = new HashMap<>();
    private static final Map<String, Keybind> registeredKeybinds = new HashMap<>();

    private static final IntSet heldVanilla = new IntOpenHashSet();
    private static final IntSet heldModules = new IntOpenHashSet();
    private static final IntSet heldRegistered = new IntOpenHashSet();
    private static final IntSet heldMacros = new IntOpenHashSet();

    private KeybindManager() {}

    // @vinzy-dev please just make this cleaner i really cant be asked to fix it thank you
    public static void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();

        Screen screen = client.currentScreen;
        if (screen != null) {
            if (screen instanceof KeybindScreen) {
                heldVanilla.clear();
                heldModules.clear();
                heldRegistered.clear();
                heldMacros.clear();
                return;
            }

            boolean isAllowedScreen = screen instanceof HandledScreen;

            if (!isAllowedScreen) {
                return;
            }

            if (screen.getFocused() != null) return;

            for (Element child : screen.children()) {
                if (child instanceof TextFieldWidget tf && tf.isFocused()) return;
            }

            if (client.options.allKeys != null) {
                for (KeyBinding kb : client.options.allKeys) {
                    if (kb.isPressed()) return;
                }
            }
        }

        long window = client.getWindow().getHandle();

        for (Map.Entry<KeyBinding, KeybindAction> entry : keybinds.entrySet()) {
            int glfwKey = ((KeyBindingAccessor) entry.getKey()).dupersunited$getBoundKey().getCode();
            if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) continue;
            int keyState = getInputState(window, glfwKey);
            if (keyState == GLFW.GLFW_PRESS && heldVanilla.add(glfwKey)) {
                entry.getValue().run();
            } else if (keyState == GLFW.GLFW_RELEASE) {
                heldVanilla.remove(glfwKey);
            }
        }

        for (Module m : MainClient.MODULE_MANAGER.getModules()) {
            int glfwKey = m.getKeybind();
            if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) continue;
            int keyState = getInputState(window, glfwKey);
            if (keyState == GLFW.GLFW_PRESS && heldModules.add(glfwKey)) {
                m.toggle();
            } else if (keyState == GLFW.GLFW_RELEASE) {
                heldModules.remove(glfwKey);
            }
        }

        for (Keybind keybind : registeredKeybinds.values()) {
            int glfwKey = keybind.getKeyCode();
            if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) continue;
            int keyState = getInputState(window, glfwKey);
            if (keyState == GLFW.GLFW_PRESS && heldRegistered.add(glfwKey)) {
                keybind.onPress();
            } else if (keyState == GLFW.GLFW_RELEASE) {
                heldRegistered.remove(glfwKey);
            }
        }

//      for (GuiMacro.Macro macro : GuiMacro.getRegisteredMacros().values()) {
//          int glfwKey = macro.key();
//          if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) continue;
//          int keyState = getInputState(window, glfwKey);
//          if (keyState == GLFW.GLFW_PRESS && heldMacros.add(glfwKey)) {
//              String macroName = macro.name();
//              if (MacroManager.isRunning() && MacroManager.getRunningName().equals(macroName)) {
//                  MacroManager.stop();
//                  sendMessage(Text.literal("Macro ")
//                      .append(Text.literal(macroName).formatted(Formatting.AQUA))
//                      .append(" stopped."), true);
//              } else {
//                  GuiMacro.runMacro(macro);
//                  sendMessage(Text.literal("Macro ")
//                      .append(Text.literal(macroName).formatted(Formatting.AQUA))
//                      .append(" started."), true);
//              }
//          } else if (keyState == GLFW.GLFW_RELEASE) {
//              heldMacros.remove(glfwKey);
//          }
//      }
    }

    private static int getInputState(long window, int code) {
        if (code < 0) {
            int mouseButton = (-code) - 100;
            if (mouseButton >= GLFW.GLFW_MOUSE_BUTTON_1 && mouseButton <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                return GLFW.glfwGetMouseButton(window, mouseButton);
            }
            return GLFW.GLFW_RELEASE;
        }
        if (code >= GLFW.GLFW_MOUSE_BUTTON_1 && code <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            return GLFW.glfwGetMouseButton(window, code);
        }
        return GLFW.glfwGetKey(window, code);
    }

    public static void addKeybind(KeyBinding key, Runnable onPress) {
        if (onPress != null) keybinds.put(key, onPress::run);
    }

    public static void registerKeybind(Keybind keybind) {
        registeredKeybinds.put(keybind.getId(), keybind);
    }

    public static void unregisterKeybind(String id) {
        registeredKeybinds.remove(id);
    }

    public static Map<KeyBinding, KeybindAction> getKeybinds() {
        return keybinds;
    }

    public static Map<String, Keybind> getRegisteredKeybinds() {
        return registeredKeybinds;
    }

    @FunctionalInterface
    public interface KeybindAction {
        void run();
    }
}