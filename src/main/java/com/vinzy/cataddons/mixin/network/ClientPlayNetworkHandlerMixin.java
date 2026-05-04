package com.vinzy.cataddons.mixin.network;

import com.mojang.brigadier.tree.RootCommandNode;
import com.vinzy.cataddons.commands.subcommands.NewCommandsCommand;
import com.vinzy.cataddons.features.PluginScanner;
import com.vinzy.cataddons.features.TPSDisplay;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow @Final private static CommandTreeS2CPacket.NodeFactory<ClientCommandSource> COMMAND_NODE_FACTORY;
    @Shadow @Final private DynamicRegistryManager.Immutable combinedDynamicRegistries;
    @Shadow @Final private FeatureSet enabledFeatures;

    @Inject(method = "onWorldTimeUpdate", at = @At("TAIL"))
    private void cataddons$onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        TPSDisplay.onWorldTimeUpdate();
    }

    @Inject(method = "onCommandSuggestions", at = @At("TAIL"))
    private void cataddons$onCommandSuggestions(CommandSuggestionsS2CPacket packet, CallbackInfo ci) {
        PluginScanner.onCommandSuggestions(packet);
    }

    @Inject(method = "onCommandTree", at = @At("TAIL"))
    private void cataddons$onCommandTree(CommandTreeS2CPacket packet, CallbackInfo ci) {
        // create a copy of the command tree so that fabric's client command api doesn't touch it
        RootCommandNode<ClientCommandSource> rootNode = packet.getCommandTree(
            CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures),
            COMMAND_NODE_FACTORY
        );

        PluginScanner.onCommandTree(rootNode);
        NewCommandsCommand.onCommandTree(rootNode);
    }
}