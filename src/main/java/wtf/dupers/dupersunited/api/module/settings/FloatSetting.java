package wtf.dupers.dupersunited.api.module.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class FloatSetting extends Setting<Float> {
    private final float min, max;

    public FloatSetting(String name, float defaultValue, float min, float max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean setValue(Float value) {
        if (value >= min && value <= max) {
            return super.setValue(value);
        } else {
            return false;
        }
    }

    public float getMin() { return min; }
    public float getMax() { return max; }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(this.getValue());
    }

    @Override
    public void readJson(JsonElement element) throws IllegalArgumentException {
        this.setValue(element.getAsFloat());
    }
}
