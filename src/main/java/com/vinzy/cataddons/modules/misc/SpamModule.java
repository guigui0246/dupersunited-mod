package com.vinzy.cataddons.modules.misc;

import com.vinzy.cataddons.modules.Category;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.BindSetting;
import com.vinzy.cataddons.modules.settings.BooleanSetting;
import com.vinzy.cataddons.modules.settings.IntSetting;
import com.vinzy.cataddons.modules.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SpamModule extends Module {

    private static final int MAX_MESSAGES = 3;

    private final StringSetting[] messages = new StringSetting[MAX_MESSAGES];
    private final IntSetting delay = register(new IntSetting("Delay (ms)", 1000, 50, 10000));
    private final BooleanSetting limited = register(new BooleanSetting("Limit", false));
    private final StringSetting count = register(new StringSetting("Count", "10"));

    private long lastSendTime = 0;
    private int sentCount = 0;
    private int messageIndex = 0;

    public SpamModule() {
        super("Spam", "Repeatedly sends chat messages with a configurable delay.", Category.misc);

        // defualt messages
        for (int i = 0; i < MAX_MESSAGES; i++) {
            String defaultVal = i == 0 ? "DU ON TOP" : "JOIN GG/DUPES";
            messages[i] = register(new StringSetting("MSG " + (i + 1), defaultVal, 256));
        }

        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));

        count.visible = () -> limited.getValue();
    }

    @Override
    protected void onEnable() {
        lastSendTime = 0;
        sentCount = 0;
        messageIndex = 0;
    }

    @Override
    protected void onDisable() {
        sentCount = 0;
        messageIndex = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (limited.getValue() && sentCount >= getCountValue()) {
            setEnabled(false);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSendTime < delay.getValue()) return;

        List<String> active = getActiveMessages();
        if (active.isEmpty()) return;

        if (messageIndex >= active.size()) messageIndex = 0;
        mc.player.networkHandler.sendChatMessage(active.get(messageIndex));

        messageIndex = (messageIndex + 1) % active.size();
        lastSendTime = now;
        sentCount++;
    }

    // returning only non empty message slots
    private List<String> getActiveMessages() {
        List<String> result = new ArrayList<>();
        for (StringSetting m : messages) {
            String val = m.getValue();
            if (val != null && !val.isEmpty()) result.add(val);
        }
        return result;
    }

    private int getCountValue() {
        try {
            int val = Integer.parseInt(count.getValue().trim());
            return Math.max(1, val);
        } catch (NumberFormatException e) {
            return 10;
        }
    }
}