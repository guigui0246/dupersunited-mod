package wtf.dupers.dupersunited.api.module.settings;

public class ButtonSetting extends Setting<String> {

    private final Runnable action;

    public ButtonSetting(String name, Runnable action) {
        super(name, name);
        this.action = action;
    }

    public void press() {
        action.run();
    }
}