package wtf.dupers.dupersunited.api.module.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

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
    public boolean setValue(String value) {
        if (value == null) return false;
        if (value.length() > maxLength) {
            super.setValue(value.substring(0, maxLength));
        } else {
            super.setValue(value);
        }
        return true;
    }

    public int getMaxLength() { return maxLength; }

    @Override
    public JsonElement writeJson() {
        return new JsonPrimitive(this.getValue());
    }

    @Override
    public void readJson(JsonElement element) throws IllegalArgumentException {
        this.setValue(element.getAsString());
    }
}
