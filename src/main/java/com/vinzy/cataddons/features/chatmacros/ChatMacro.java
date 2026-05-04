package com.vinzy.cataddons.features.chatmacros;

import java.util.ArrayList;
import java.util.List;

public class ChatMacro {

    private String name;
    private List<MacroMessage> messages;
    private int keyCode;

    public ChatMacro(String name, List<MacroMessage> messages, int keyCode) {
        this.name = name;
        this.messages = new ArrayList<>(messages);
        this.keyCode = keyCode;
    }

    public String getName() {
        return name;
    }
    public void   setName(String name) {
        this.name = name;
    }

    public List<MacroMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<MacroMessage> m) {
        this.messages = new ArrayList<>(m);
    }

    public int  getKeyCode() {
        return keyCode;
    }
    public void setKeyCode(int key) {
        this.keyCode = key;
    }
}