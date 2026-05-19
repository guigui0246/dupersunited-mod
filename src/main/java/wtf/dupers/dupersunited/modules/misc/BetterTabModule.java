package wtf.dupers.dupersunited.modules.misc;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.BooleanSetting;
import wtf.dupers.dupersunited.modules.settings.IntSetting;
import wtf.dupers.dupersunited.utils.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

public class BetterTabModule extends Module {
    public final IntSetting tabSize = register(new IntSetting("TablistSize", 500, 1, 1000));
    public final IntSetting columnHeight = register(new IntSetting("ColumnHeight", 50, 1, 100));
    public final BooleanSetting scrollable = register(new BooleanSetting("Scrollable", false));
    public final BooleanSetting highlightSelf = register(new BooleanSetting("HighlightSelf", true));
    public final BooleanSetting showGamemode = register(new BooleanSetting("Gamemode", false));
    public final BooleanSetting showPing = register(new BooleanSetting("Ping", false));
    public final BooleanSetting showPingIcon = register(new BooleanSetting("PingIcon", true));

    public int scrollOffset = 0;

    MinecraftClient mc = MinecraftClient.getInstance();

    public BetterTabModule() {
        super("BetterTab", "Allows you to configure your tab list.", Category.misc);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    public void onMouseScroll(double amount) {
        if (!isEnabled() || !scrollable.getValue()) return;

        if (amount > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else {
            scrollOffset++;
        }
    }

    @Override
    public void onDisable() {
        scrollOffset = 0;
        super.onDisable();
    }

    public Text getPlayerName(PlayerListEntry entry) {
        Text name = entry.getDisplayName();
        if (name == null) name = Text.literal(entry.getProfile().name());

        if (highlightSelf.getValue() && entry.getProfile().id().toString()
                .equals(mc.player.getGameProfile().id().toString())) {
            name = Text.literal(name.getString()).setStyle(name.getStyle().withColor(TextColor.fromRgb(ColorUtil.GREEN)));
        }

        if (showGamemode.getValue()) {
            GameMode gm = entry.getGameMode();
            String gmText = gm == null ? "?" : switch (gm) {
                case SPECTATOR -> "Sp";
                case SURVIVAL -> "S";
                case CREATIVE -> "C";
                case ADVENTURE -> "A";
            };
            MutableText text = Text.empty();
            text.append(name);
            text.append(Text.literal(" [" + gmText + "]").setStyle(net.minecraft.text.Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))));
            name = text;
        }

        return name;
    }
}