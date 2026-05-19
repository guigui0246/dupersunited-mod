package wtf.dupers.dupersunited.features;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.glitcha.PacketLoggerModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static wtf.dupers.dupersunited.commands.MainCommand.sendMessage;

public class PacketLogger {

    public static void log(Packet<?> packet, String direction) {
        PacketLoggerModule module = (PacketLoggerModule) MainClient.MODULE_MANAGER.getModuleByName("PacketLogger");
        if (module == null || !module.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String packetName = null;
        String extraData  = "";

        if (module.logGuiClose.getValue() && (packet instanceof CloseHandledScreenC2SPacket || packet instanceof CloseScreenS2CPacket)) {
            packetName = "Close Window Packet";
        }
        else if (module.logGuiClick.getValue() && packet instanceof ClickSlotC2SPacket p) {
            packetName = "Click Window Packet";
            ItemStack stack    = mc.player.currentScreenHandler.getCursorStack();
            int       slot     = p.slot();
            String    itemName = stack.isEmpty() ? "None" : stack.getName().getString();
            extraData = String.format(" [Slot: %d, Item: %s]", slot, itemName);
        }
        else if (module.logGuiOpen.getValue() && packet instanceof OpenScreenS2CPacket p) {
            packetName = "Open Window Packet";
            String title  = p.getName().getString();
            int    syncId = p.getSyncId();
            extraData = String.format(" [Name: %s, ID: %d]", title, syncId);
        }
        else if (module.logGuiUpdates.getValue()) {
            if (packet instanceof InventoryS2CPacket p) {
                packetName = "Inventory";
                extraData  = String.format(" [ID: %d, Items: %d]", p.syncId(), p.contents().size());
            }
            else if (packet instanceof ScreenHandlerSlotUpdateS2CPacket p) {
                packetName = "Slot Update";
                ItemStack stack    = p.getStack();
                String    itemName = stack.isEmpty() ? "None" : stack.getName().getString();
                extraData = String.format(" [Slot: %d, Item: %s]", p.getSlot(), itemName);
            }
        }

        if (module.logSigns.getValue() && packet instanceof UpdateSignC2SPacket p) {
            packetName = "Update Sign Packet";
            extraData  = String.format(" [Lines: %s, %s, %s, %s]",
                    p.getText()[0], p.getText()[1], p.getText()[2], p.getText()[3]);
        }

        if (packetName == null) return;

        final Text styledAction = direction.equals("OUT") ? Text.literal("Sent ").formatted(Formatting.RED) : Text.literal("Received ").formatted(Formatting.GREEN);
        final Text styledName = Text.literal(packetName).formatted(Formatting.WHITE);
        final Text styledExtra = Text.literal(extraData).formatted(Formatting.GRAY);

        mc.execute(() -> {
            if (mc.player == null) return;
            sendMessage(Text.empty()
                .append(styledAction)
                .append(styledName)
                .append(styledExtra), true);
        });
    }
}