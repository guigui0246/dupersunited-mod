package wtf.dupers.dupersunited.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.api.command.Command;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.*;
import wtf.dupers.dupersunited.commands.MainCommand;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class ModuleCommand extends Command {
    public ModuleCommand() {
        super("module", "Shows modules current binds & settings.");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess registryAccess) {
        builder.then(argument("module", StringArgumentType.word())
            .suggests((context, suggestions) ->
                CommandSource.suggestMatching(MainClient.MODULE_MANAGER.modules().stream().map(Module::getName), suggestions))
            .executes(context -> {
                String moduleName = StringArgumentType.getString(context, "module");
                Module module = MainClient.MODULE_MANAGER.getModuleByName(moduleName);

                if (module == null) {
                    MainCommand.sendMessage(
                        Text.literal("Module not found.").formatted(Formatting.RED),
                        true
                    );
                    return 0;
                }

                var settings = module.getSettings();
                if (settings.isEmpty()) {
                    MainCommand.sendMessage(Text.empty()
                        .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                        .append(" has no settings."), true);

                    return 1;
                }

                MainCommand.sendMessage(Text.literal("Settings for ")
                    .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                    .append(":"), true);

                for (var setting : settings) {
                    MainCommand.sendMessage(Text.literal(" - ").formatted(Formatting.GRAY)
                        .append(Text.literal(setting.getName()).formatted(Formatting.DARK_AQUA))
                        .append(": ")
                        .append(Text.literal(String.valueOf(setting.getValue())).formatted(Formatting.AQUA)), true);
                }
                return 1;
            })
            .then(argument("setting", StringArgumentType.word())
                .suggests((context, suggestions) -> {
                    String moduleName = StringArgumentType.getString(context, "module");
                    Module module = MainClient.MODULE_MANAGER.getModuleByName(moduleName);
                    return CommandSource.suggestMatching(module.getSettings().stream().map(Setting::getName), suggestions);
                })
                .then(argument("value", StringArgumentType.greedyString())
                    .executes(context -> {
                        String moduleName = StringArgumentType.getString(context, "module");
                        String settingName = StringArgumentType.getString(context, "setting");
                        String value = StringArgumentType.getString(context, "value");

                        Module module = MainClient.MODULE_MANAGER.getModuleByName(moduleName);
                        if (module == null) {
                            MainCommand.sendMessage(
                                Text.literal("Module not found.").formatted(Formatting.RED),
                                true
                            );
                            return 0;
                        }

                        var setting = module.getSettingByName(settingName);
                        if (setting == null) {
                            MainCommand.sendMessage(Text.literal("Setting ")
                                .append(Text.literal(settingName).formatted(Formatting.AQUA))
                                .append(Text.literal(" not found").formatted(Formatting.RED))
                                .append(" on ")
                                .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                                .append("."), true);

                            return 0;
                        }

                        try {
                            switch (setting) {
                                case FloatSetting fs -> fs.setValue(Float.parseFloat(value));
                                case BooleanSetting bs -> bs.setValue(Boolean.parseBoolean(value));
                                case ModeSetting ms -> ms.setValue(value);
                                case EnumSetting<?> es -> es.setValue(value);
                                case StringSetting ss -> ss.setValue(value);
                                default -> {
                                    MainCommand.sendMessage(
                                        Text.literal("Unsupported setting type.").formatted(Formatting.RED),
                                        true
                                    );
                                    return 0;
                                }
                            }

                            MainCommand.sendMessage(Text.empty()
                                .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                                .append(" setting ")
                                .append(Text.literal(settingName).formatted(Formatting.AQUA))
                                .append(" set to ")
                                .append(Text.literal(String.valueOf(setting.getValue())).formatted(Formatting.AQUA))
                                .append("."), true);

                            return 1;
                        } catch (NumberFormatException e) {
                            MainCommand.sendMessage(Text.literal("Invalid value ").formatted(Formatting.RED)
                                .append(Text.literal(value).formatted(Formatting.WHITE))
                                .append(" for setting ")
                                .append(Text.literal(settingName).formatted(Formatting.AQUA))
                                .append("."), true);

                            return 0;
                        }
                    })
                )
            )
        );
    }
}