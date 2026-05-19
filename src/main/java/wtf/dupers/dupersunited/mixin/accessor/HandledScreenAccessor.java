package wtf.dupers.dupersunited.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x") int dupersunited$getGuiX();
    @Accessor("y") int dupersunited$getGuiY();
    @Accessor("backgroundWidth") int dupersunited$getBackgroundWidth();
    @Accessor("backgroundHeight") int dupersunited$getBackgroundHeight();
}