package wtf.dupers.dupersunited.api.module.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final List<T> options;

    public EnumSetting(String name, T defaultValue) {
        super(name, defaultValue);
        this.options = Arrays.asList(defaultValue.getDeclaringClass().getEnumConstants());
    }

    public void setValue(String stringifiedValue) {
        for (T option : this.options) {
            if (option.toString().equals(stringifiedValue)) {
                this.setValue(option);
            }
        }
    }

    public void cycle(boolean forwards) {
        int idx = this.options.indexOf(this.getValue());
        if (forwards) idx = (idx + 1) % this.options.size();
        else idx = (idx - 1 + this.options.size()) % this.options.size();
        this.setValue(this.options.get(idx));
    }

    public List<T> getOptions() {
        return this.options;
    }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(this.getValue().toString());
    }

    @Override
    public void readJson(JsonElement element) throws IllegalArgumentException {
        this.setValue(element.getAsString());
    }
}
