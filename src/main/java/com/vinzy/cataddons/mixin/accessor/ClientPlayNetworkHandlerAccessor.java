package com.vinzy.cataddons.mixin.accessor;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {

    @Accessor("combinedDynamicRegistries")
    DynamicRegistryManager.Immutable cataddons$getCombinedDynamicRegistries();

    @Accessor("enabledFeatures")
    FeatureSet cataddons$getEnabledFeatures();

    @Accessor("COMMAND_NODE_FACTORY")
    static CommandTreeS2CPacket.NodeFactory<?> cataddons$getCommandNodeFactory() { throw new AssertionError(); }
}