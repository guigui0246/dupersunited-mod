package com.vinzy.cataddons.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.Module;
import com.vinzy.cataddons.modules.settings.*;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ModuleCommand {
    private ModuleCommand() {}

    public static String getDescription() {
        return "Shows modules current binds & settings.";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("module")
                .then(argument("module", StringArgumentType.word())
                        .suggests((context, builder) ->
                            CommandSource.suggestMatching(MainClient.MODULE_MANAGER.getModules().stream().map(Module::getName), builder))
                        .executes(context -> {
                            String moduleName = StringArgumentType.getString(context, "module");
                            Module module = MainClient.MODULE_MANAGER.getModuleByName(moduleName);

                            if (module == null) {
                                CommandCat.sendMessage(
                                    Text.literal("Module not found.").formatted(Formatting.RED),
                                    true
                                );
                                return 0;
                            }

                            var settings = module.getSettings();
                            if (settings.isEmpty()) {
                                CommandCat.sendMessage(Text.empty()
                                    .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                                    .append(" has no settings."), true);

                                return 1;
                            }

                            CommandCat.sendMessage(Text.literal("Settings for ")
                                .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                                .append(":"), true);

                            for (var setting : settings) {
                                CommandCat.sendMessage(Text.literal(" - ").formatted(Formatting.GRAY)
                                    .append(Text.literal(setting.getName()).formatted(Formatting.DARK_AQUA))
                                    .append(": ")
                                    .append(Text.literal(String.valueOf(setting.getValue())).formatted(Formatting.AQUA)), true);
                            }
                            return 1;
                        })
                        .then(argument("setting", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String moduleName = StringArgumentType.getString(context, "module");
                                    Module module = MainClient.MODULE_MANAGER.getModuleByName(moduleName);
                                    return CommandSource.suggestMatching(module.getSettings().stream().map(Setting::getName), builder);
                                })
                                .then(argument("value", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String moduleName = StringArgumentType.getString(context, "module");
                                            String settingName = StringArgumentType.getString(context, "setting");
                                            String value = StringArgumentType.getString(context, "value");

                                            Module module = MainClient.MODULE_MANAGER.getModuleByName(moduleName);
                                            if (module == null) {
                                                CommandCat.sendMessage(
                                                    Text.literal("Module not found.").formatted(Formatting.RED),
                                                    true
                                                );
                                                return 0;
                                            }

                                            var setting = module.getSettingByName(settingName);
                                            if (setting == null) {
                                                CommandCat.sendMessage(Text.literal("Setting ")
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
                                                    case StringSetting ss -> ss.setValue(value);
                                                    default -> {
                                                        CommandCat.sendMessage(
                                                            Text.literal("Unsupported setting type.").formatted(Formatting.RED),
                                                            true
                                                        );
                                                        return 0;
                                                    }
                                                }

                                                CommandCat.sendMessage(Text.empty()
                                                    .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                                                    .append(" setting ")
                                                    .append(Text.literal(settingName).formatted(Formatting.AQUA))
                                                    .append(" set to ")
                                                    .append(Text.literal(String.valueOf(setting.getValue())).formatted(Formatting.AQUA))
                                                    .append("."), true);

                                                return 1;
                                            } catch (NumberFormatException e) {
                                                CommandCat.sendMessage(Text.literal("Invalid value ").formatted(Formatting.RED)
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