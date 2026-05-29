package wtf.dupers.dupersunited.modules.glitcha;

import wtf.dupers.dupersunited.features.PacketPauseManager;
import wtf.dupers.dupersunited.features.screens.PacketDelayScreen;
import wtf.dupers.dupersunited.api.module.Category;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.api.module.settings.BindSetting;
import wtf.dupers.dupersunited.api.module.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.*;
import net.minecraft.network.packet.c2s.play.*;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class PacketDelayModule extends Module {
    private final Map<BooleanSetting, Class<? extends Packet<?>>> packetSettings = new LinkedHashMap<>();
    private boolean lastMasterState = false;
    private boolean selectiveMode = false;

    public static final BindSetting blinkBind = new BindSetting("DelayPackets", GLFW.GLFW_KEY_F7);

    public PacketDelayModule() {
        super("DelayPackets", "Let's you customize which packets get disabled.", Category.glitcha);
        this.register(new BindSetting("GUI Key", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
        this.register(blinkBind);

        // movement packets
        addPacket("Position", PlayerMoveC2SPacket.PositionAndOnGround.class);
        addPacket("Rotation", PlayerMoveC2SPacket.LookAndOnGround.class);
        addPacket("PositionLook", PlayerMoveC2SPacket.Full.class);
        addPacket("GroundOnly", PlayerMoveC2SPacket.OnGroundOnly.class);
        addPacket("VehicleMove", VehicleMoveC2SPacket.class);
        addPacket("PlayerInput", PlayerInputC2SPacket.class);
        addPacket("BoatPaddle", BoatPaddleStateC2SPacket.class);
        addPacket("TeleportConfirm", TeleportConfirmC2SPacket.class);
        addPacket("TickEnd", ClientTickEndC2SPacket.class);

        // interaction
        addPacket("PlayerAction", PlayerActionC2SPacket.class);
        addPacket("InteractBlock", PlayerInteractBlockC2SPacket.class);
        addPacket("InteractEntity", PlayerInteractEntityC2SPacket.class);
        addPacket("InteractItem", PlayerInteractItemC2SPacket.class);
        addPacket("HandSwing", HandSwingC2SPacket.class);
        addPacket("ClientCommand", ClientCommandC2SPacket.class);
        addPacket("UpdateSlot", UpdateSelectedSlotC2SPacket.class);

        // inventory
        addPacket("ClickSlot", ClickSlotC2SPacket.class);
        addPacket("CloseScreen", CloseHandledScreenC2SPacket.class);
        addPacket("ButtonClick", ButtonClickC2SPacket.class);
        addPacket("CreativeAction", CreativeInventoryActionC2SPacket.class);
        addPacket("CraftRequest", CraftRequestC2SPacket.class);
        addPacket("RecipeBookData", RecipeBookDataC2SPacket.class);
        addPacket("RecipeCategory", RecipeCategoryOptionsC2SPacket.class);

        // chat, command & data stuff
        addPacket("ChatMessage", ChatMessageC2SPacket.class);
        addPacket("CommandExec", CommandExecutionC2SPacket.class);
        addPacket("UpdateSign", UpdateSignC2SPacket.class);
        addPacket("UpdateCommandBlock", UpdateCommandBlockC2SPacket.class);
        addPacket("UpdateBeacon", UpdateBeaconC2SPacket.class);
        addPacket("BookUpdate", BookUpdateC2SPacket.class);
        addPacket("RenameItem", RenameItemC2SPacket.class);

        // technical packets
        addPacket("KeepAlive", KeepAliveC2SPacket.class);
        addPacket("ClientStatus", ClientStatusC2SPacket.class);
        addPacket("CustomPayload", CustomPayloadC2SPacket.class);
        addPacket("AdvancementTab", AdvancementTabC2SPacket.class);
        addPacket("ResourcePackStatus", ResourcePackStatusC2SPacket.class);
        addPacket("QueryBlockNbt", QueryBlockNbtC2SPacket.class);
        addPacket("ChunkAck", AcknowledgeChunksC2SPacket.class);
        addPacket("ReconfigAck", AcknowledgeReconfigurationC2SPacket.class);

        fillAll();
    }

    private void addPacket(String name, Class<? extends Packet<?>> clazz) {
        // None of these get registered to clickgui as that's a fucking cancer show it's too many things :(
        packetSettings.put(new BooleanSetting(name, true), clazz);
    }

    @Override
    public void toggle() {
        // Doing this so I can override the base toggle to avoid annoying fucking toggle message
        setEnabled(!isEnabled());
    }

    @Override
    protected void onEnable() {
        // open the screen immediately when the module is toggled
        MinecraftClient mc = MinecraftClient.getInstance();
        Screen previous = mc.currentScreen;
        mc.execute(() -> mc.setScreen(new PacketDelayScreen(previous, this)));
        // now we turn the module back off so it acts as a button & not spam shit with the module enable/disabled
        this.setEnabled(false);
    }

    @Override protected void onDisable() { fillAll(); }

    public void syncTargets() {
        PacketPauseManager.clearTargets();
        if (!selectiveMode) {
            packetSettings.values().forEach(PacketPauseManager::addTarget);
        } else {
            packetSettings.forEach((setting, clazz) -> {
                if (!setting.getValue()) PacketPauseManager.addTarget(clazz);
            });
        }
    }

    public void resetSettings() {
        packetSettings.keySet().forEach(s -> s.setValue(true));
    }

    private void fillAll() {
        PacketPauseManager.clearTargets();
        packetSettings.values().forEach(PacketPauseManager::addTarget);
    }

    public boolean isSelectiveMode() { return selectiveMode; }
    public void toggleSelectiveMode() { selectiveMode = !selectiveMode; }
    public Map<BooleanSetting, Class<? extends Packet<?>>> getPacketSettings() { return packetSettings; }
}