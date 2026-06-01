package wtf.dupers.dupersunited.api;

import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.keybind.Keybind;

/**
 * The interface through which you register features into the DupersUnited mod.
 */
public interface DupersUnitedRegistry {
    void registerCommand(Command command);

    default void registerCommands(Command... commands) {
        for (Command command : commands) this.registerCommand(command);
    }

    void registerModule(Module module);

    default void registerModules(Module... modules) {
        for (Module module : modules) this.registerModule(module);
    }

    void registerKeybind(Keybind keybind);

    default void registerKeybinds(Keybind... keybinds) {
        for (Keybind keybind : keybinds) this.registerKeybind(keybind);
    }
}
