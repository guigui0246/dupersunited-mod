package wtf.dupers.dupersunited.api.module.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntSetting extends Setting<Integer> {
    private final int min, max;

    public IntSetting(String name, int defaultValue, int min, int max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }

    public int getMin() { return min; }
    public int getMax() { return max; }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(this.getValue());
    }

    @Override
    public void readJson(JsonElement element) throws IllegalArgumentException {
        this.setValue(element.getAsInt());
    }
}