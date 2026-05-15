package wtf.dupers.dupersunited.mixin.misc;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.misc.ChatStackerModule;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Shadow @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Unique private static final int STACK_RECENCY_LINES = 12;
    @Unique private final Map<Text, ChatStackerModule.RepeatingMessage> messages = new HashMap<>();
    @Unique private volatile boolean wasLastMessageBlank = false; // ChatHud isn't thread-safe but mods dont give a fuck

    @Inject(method = "addVisibleMessage", at = @At("HEAD"), cancellable = true)
    public void dupersunited$addVisibleMessage(ChatHudLine message, CallbackInfo ci, @Share("message") LocalRef<MutableText> messageRef, @Share("messageData") LocalRef<ChatStackerModule.RepeatingMessage> messageDataRef) {
        if (MainClient.MODULE_MANAGER.isEnabled(ChatStackerModule.class)) {
            String plainText = message.content().getString().strip();

            if (plainText.matches("[\\-=+*_~]+")) { // avoid touching separators
                return;
            }

            if (plainText.isBlank()) {
                // wipe duplicate blank lines
                if (wasLastMessageBlank) {
                    ci.cancel();
                }
                wasLastMessageBlank = true;
            } else {
                wasLastMessageBlank = false;
            }

            ChatStackerModule.RepeatingMessage messageData = messages.get(message.content());

            if (messageData != null && dupersunited$isRecent(messageData)) {
                visibleMessages.removeAll(messageData.instances());
                messageData.instances().clear();

                messageRef.set(messageData.originalMessage()
                    .copy()
                    .append(" ")
                    .append(Text.literal("(" + messageData.count().incrementAndGet() + ")")
                        .setStyle(Style.EMPTY.withColor(Formatting.GRAY))));
            } else {
                messageData = new ChatStackerModule.RepeatingMessage(message.content().copy(), new ArrayList<>(), new MutableInt(1));
                messages.put(message.content(), messageData);
            }

            messageDataRef.set(messageData);
        }
    }

    @Unique
    private boolean dupersunited$isRecent(ChatStackerModule.RepeatingMessage msg) {
        if (msg.instances().isEmpty()) return false;
        // check if any of its visible lines are within the recency window
        for (ChatHudLine.Visible instance : msg.instances()) {
            int idx = visibleMessages.indexOf(instance);
            if (idx >= 0 && idx < STACK_RECENCY_LINES) return true;
        }
        return false;
    }

    @ModifyArg(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"), index = 0)
    public StringVisitable dupersunited$addMessage(StringVisitable message, @Share("message") LocalRef<MutableText> overriddenMessage) {
        return overriddenMessage.get() != null ? overriddenMessage.get() : message;
    }

    @Redirect(method = "addVisibleMessage", at = @At(value = "NEW", target = "(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;"))
    public ChatHudLine.Visible dupersunited$createOrderedText(int i, OrderedText orderedText, MessageIndicator messageIndicator, boolean bl, @Share("messageData") LocalRef<ChatStackerModule.RepeatingMessage> messageData) {
        ChatHudLine.Visible visible = new ChatHudLine.Visible(i, orderedText, messageIndicator, bl);
        if (messageData.get() != null) messageData.get().instances().add(visible);
        return visible;
    }
}