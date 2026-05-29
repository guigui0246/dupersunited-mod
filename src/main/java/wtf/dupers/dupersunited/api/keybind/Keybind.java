package wtf.dupers.dupersunited.api.keybind;

public abstract class Keybind {
    private final String name;
    private int keyCode;

    public Keybind(String name, int defaultKey) {
        this.name = name;
        this.keyCode = defaultKey;
    }

    public String getName() {
        return name;
    }
    public int getKeyCode() {
        return keyCode;
    }
    public void setKeyCode(int key) {
        this.keyCode = key;
    }

    public abstract void onPress();
}