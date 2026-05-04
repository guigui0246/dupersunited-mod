package com.vinzy.cataddons.modules.settings;

public class FloatSetting extends Setting<Float> {
    private final float min, max;

    public FloatSetting(String name, float defaultValue, float min, float max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void setValue(Float value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
}
