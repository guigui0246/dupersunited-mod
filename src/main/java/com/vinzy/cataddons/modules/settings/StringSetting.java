package com.vinzy.cataddons.modules.settings;

public class StringSetting extends Setting<String> {

    private final int maxLength;

    public StringSetting(String name, String defaultValue) {
        super(name, defaultValue);
        this.maxLength = Integer.MAX_VALUE;
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name, defaultValue);
        this.maxLength = maxLength;
    }

    @Override
    public void setValue(String value) {
        if (value == null) return;
        if (value.length() > maxLength) {
            super.setValue(value.substring(0, maxLength));
        } else {
            super.setValue(value);
        }
    }

    public int getMaxLength() { return maxLength; }
}
