package com.vinzy.cataddons.keybinds;

import com.vinzy.cataddons.features.ServerInviteManager;
import org.lwjgl.glfw.GLFW;

public class JoinServerInviteKeybind extends Keybind {
    public static final JoinServerInviteKeybind INSTANCE = new JoinServerInviteKeybind();

    private JoinServerInviteKeybind() {
        super("Join Server Invite", GLFW.GLFW_KEY_Y);
    }

    @Override
    public void onPress() {
        ServerInviteManager.joinActiveInvite();
    }
}