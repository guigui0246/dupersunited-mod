package wtf.dupers.dupersunited.mixin.accessor;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {

    @Accessor("combinedDynamicRegistries")
    DynamicRegistryManager.Immutable dupersunited$getCombinedDynamicRegistries();

    @Accessor("enabledFeatures")
    FeatureSet dupersunited$getEnabledFeatures();

    @Accessor("COMMAND_NODE_FACTORY")
    static CommandTreeS2CPacket.NodeFactory<?> dupersunited$getCommandNodeFactory() { throw new AssertionError(); }
}