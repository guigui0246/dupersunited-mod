package wtf.dupers.dupersunited.modules.misc;

import wtf.dupers.dupersunited.mixin.accessor.PlayerMoveC2SPacketAccessor;
import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFallModule extends Module {
    public NoFallModule() {
        super("NoFall", "Disables fall damage.", Category.misc);
    }

    @Override
    public void onPacketSend(Packet<?> packet) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!(packet instanceof PlayerMoveC2SPacket)) return;
        if (mc.player.getAbilities().creativeMode) return;
        if (mc.player.isGliding()) return;
        if (mc.player.getVelocity().y > -0.5) return;

        ((PlayerMoveC2SPacketAccessor) packet).dupersunited$setOnGround(true);
    }
}