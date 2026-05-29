package wtf.dupers.dupersunited.api;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.api.module.Module;

import java.util.Collection;
import java.util.Optional;

public interface DupersUnitedMod {
    static Collection<Module> getModules() {
        return MainClient.getModules();
    }

    static <T extends Module> Optional<T> getModule(Class<T> moduleClass) {
        return Optional.ofNullable(MainClient.getModule(moduleClass));
    }

    static Optional<Module> getModule(String moduleName) {
        return Optional.ofNullable(MainClient.getModule(moduleName));
    }

    static Collection<Command> getCommands() {
        return MainClient.getCommands();
    }

    static <T extends Command> Optional<T> getCommand(Class<T> commandClass) {
        return Optional.ofNullable(MainClient.getCommand(commandClass));
    }

    static Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(MainClient.getCommand(commandName));
    }
}
