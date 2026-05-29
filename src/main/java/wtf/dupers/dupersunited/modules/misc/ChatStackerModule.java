package wtf.dupers.dupersunited.modules.misc;

import wtf.dupers.dupersunited.api.module.Category;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.BindSetting;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ChatStackerModule extends Module {
    public ChatStackerModule() {
        super("ChatStacker", "Stacks multiple of the same chat into 1 line.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    public record RepeatingMessage(Text originalMessage, ArrayList<ChatHudLine.Visible> instances, MutableInt count) {}
}