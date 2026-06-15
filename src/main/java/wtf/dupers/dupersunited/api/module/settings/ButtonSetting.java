package wtf.dupers.dupersunited.api.module.settings;

import com.google.gson.JsonElement;

public class ButtonSetting extends Setting<String> {

    private final Runnable action;

    public ButtonSetting(String name, Runnable action) {
        super(name, name);
        this.action = action;
    }

    public void press() {
        action.run();
    }

    @Override
    public boolean shouldSaveConfig() {
        return false;
    }

    @Override
    public JsonElement writeJson() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readJson(JsonElement element) {
        throw new UnsupportedOperationException();
    }
}