package com.vinzy.cataddons.modules.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private List<String> options;

    public ModeSetting(String name, String defaultValue, String... options) {
        super(name, defaultValue);
        this.options = Arrays.asList(options);
    }

    @Override
    public void setValue(String value) {
        if (options.contains(value)) super.setValue(value);
    }

    public void setOptions(String... newOptions) {
        options = Arrays.asList(newOptions);
        if (!options.contains(getValue())) {
            super.setValue(options.get(0));
        }
    }

    public List<String> getOptions() { return options; }
}