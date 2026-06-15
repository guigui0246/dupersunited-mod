package wtf.dupers.dupersunited.api.module.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private List<String> options;

    public ModeSetting(String name, String defaultValue, String... options) {
        super(name, defaultValue);
        this.options = Arrays.asList(options);
    }

    @Override
    public boolean setValue(String value) {
        if (options.contains(value)) {
            return super.setValue(value);
        } else {
            return false;
        }
    }

    public void setOptions(String... newOptions) {
        options = Arrays.asList(newOptions);
        if (!options.contains(getValue())) {
            super.setValue(options.getFirst());
        }
    }

    public List<String> getOptions() { return options; }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(this.getValue());
    }

    @Override
    public void readJson(JsonElement element) throws IllegalArgumentException {
        this.setValue(element.getAsString());
    }
}