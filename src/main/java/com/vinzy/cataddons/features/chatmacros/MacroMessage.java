package com.vinzy.cataddons.features.chatmacros;

public class MacroMessage {

    private String text;
    private int delayMs;

    public MacroMessage(String text, int delayMs) {
        this.text = text;
        this.delayMs = delayMs;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public int  getDelayMs() {
        return delayMs;
    }
    public void setDelayMs(int delayMs) {
        this.delayMs = Math.max(0, delayMs);
    }
}