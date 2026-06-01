package wtf.dupers.dupersunited.api.module;

import wtf.dupers.dupersunited.commands.MainCommand;
import wtf.dupers.dupersunited.api.module.settings.Setting;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.network.packet.Packet;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Module {
    private final String name;
    private final String category;
    private final String description;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();
    public String namespace;

    public Module(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public String getDescription() {
        return description;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public Setting<?> getSettingByName(String name) {
        return settings.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return namespace + ":" + name;
    }

    public String getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean state) {
        if (enabled == state) return;
        enabled = state;
        if (enabled) onEnable();
        else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);

        Text status = enabled
            ? Text.literal("enabled").formatted(Formatting.GREEN)
            : Text.literal("disabled").formatted(Formatting.RED);

        MainCommand.sendMessage(Text.empty()
            .append(Text.literal(name).formatted(Formatting.AQUA))
            .append(" is now ")
            .append(status)
            .append("."), true);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }

    public void onPacketSend(Packet<?> packet) {
    }

    public void onPacketReceive(Packet<?> packet) {
    }


    private int keybind = GLFW.GLFW_KEY_UNKNOWN;

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int key) {
        this.keybind = key;
    }
}