package com.vinzy.cataddons.commands.subcommands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vinzy.cataddons.commands.CommandCat;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.Module;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ToggleCommand {
    private ToggleCommand() {}

    public static String getDescription() {
        return "Toggles any module";
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("toggle")
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

                            module.toggle();

                            Text status = module.isEnabled()
                                ? Text.literal("enabled").formatted(Formatting.GREEN)
                                : Text.literal("disabled").formatted(Formatting.RED);

                            CommandCat.sendMessage(Text.empty()
                                .append(Text.literal(moduleName).formatted(Formatting.AQUA))
                                .append(" is now ")
                                .append(status)
                                .append("."), true);

                            return 1;
                        })
                );
    }
}