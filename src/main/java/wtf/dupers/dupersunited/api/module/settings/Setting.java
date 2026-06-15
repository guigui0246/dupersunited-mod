package wtf.dupers.dupersunited.api.module.settings;


import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    public BooleanSupplier visible = () -> true;
    protected T value;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }

    public boolean shouldSaveConfig() { return true; }
    public abstract JsonElement writeJson();
    public abstract void readJson(JsonElement element) throws IllegalArgumentException;
}
