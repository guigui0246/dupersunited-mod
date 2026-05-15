package wtf.dupers.dupersunited.modules.misc;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
import wtf.dupers.dupersunited.modules.settings.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class InvDropModule extends Module {

    private final IntSetting delay = register(new IntSetting("Delay (ms)", 500, 0, 1000));
    private final BooleanSetting dropArmour = register(new BooleanSetting("Drop Armour", false));
    private final BooleanSetting dropOffhand = register(new BooleanSetting("Drop Offhand", false));
    private final BooleanSetting hotbarOnly = register(new BooleanSetting("Hotbar Only", false));

    private boolean dropping = false;
    private int currentSlot = 0;
    private long lastDropTime = 0;

    public InvDropModule() {
        super("DropAll", "Drops every item in your inventory with a configurable delay.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onEnable() {
        dropping = true;
        currentSlot = 0;
        lastDropTime = 0;
    }

    @Override
    protected void onDisable() {
        dropping = false;
        currentSlot = 0;
    }

    @Override
    public void onTick() {
        if (!dropping) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        long now = System.currentTimeMillis();
        if (now - lastDropTime < delay.getValue()) return;

        int maxSlot = hotbarOnly.getValue() ? 9 : 36;

        while (currentSlot < maxSlot && mc.player.getInventory().getStack(currentSlot).isEmpty()) {
            currentSlot++;
        }

        if (currentSlot >= maxSlot) {
            if (!hotbarOnly.getValue() && dropArmour.getValue()) {
                dropArmorSlots(mc);
            }
            if (dropOffhand.getValue()) {
                dropOffhandSlot(mc);
            }
            dropping = false;
            setEnabled(false);
            return;
        }

        int screenSlot = currentSlot < 9 ? 36 + currentSlot : currentSlot;

        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                screenSlot,
                1,
                SlotActionType.THROW,
                mc.player
        );

        lastDropTime = now;
        currentSlot++;
    }

    private void dropArmorSlots(MinecraftClient mc) {
        for (int armorScreen = 5; armorScreen <= 8; armorScreen++) {
            if (!mc.player.playerScreenHandler.getSlot(armorScreen).getStack().isEmpty()) {
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        armorScreen,
                        1,
                        SlotActionType.THROW,
                        mc.player
                );
            }
        }
    }

    private void dropOffhandSlot(MinecraftClient mc) {
        if (!mc.player.playerScreenHandler.getSlot(45).getStack().isEmpty()) {
            mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId,
                    45,
                    1,
                    SlotActionType.THROW,
                    mc.player
            );
        }
    }
}