package com.vinzy.cataddons.mixin.accessor;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBoxWidget.class)
public interface EditBoxWidgetAccessor {

    @Invoker("<init>")
    static EditBoxWidget cataddons$create(
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            int height,
            Text placeholder,
            Text message,
            int textColor,
            boolean textShadow,
            int cursorColor,
            boolean hasBackground,
            boolean hasOverlay
    ) {
        throw new AssertionError();
    }
}
