package com.vinzy.cataddons.keybinds;

public abstract class Keybind {

    private final String id;
    private int keyCode;

    public Keybind(String id, int defaultKey) {
        this.id = id;
        this.keyCode = defaultKey;
    }

    public String getId() {
        return id;
    }
    public int getKeyCode() {
        return keyCode;
    }
    public void setKeyCode(int key) {
        this.keyCode = key;
    }

    public abstract void onPress();
}