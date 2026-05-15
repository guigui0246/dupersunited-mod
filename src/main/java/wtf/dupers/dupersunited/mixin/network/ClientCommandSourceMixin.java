package wtf.dupers.dupersunited.mixin.network;

import com.mojang.brigadier.suggestion.Suggestions;
import wtf.dupers.dupersunited.utils.IClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ClientCommandSource.class)
public class ClientCommandSourceMixin implements IClientCommandSource {
    @Shadow private @Nullable CompletableFuture<Suggestions> pendingCommandCompletion;
    @Shadow private int completionId;

    @Override
    public int dupersunited$beginCompletion() {
        if (this.pendingCommandCompletion != null) {
            this.pendingCommandCompletion.cancel(false);
        }

        this.pendingCommandCompletion = new CompletableFuture<>();
        return ++this.completionId;
    }

    @Override
    public void dupersunited$endCompletion() {
        this.pendingCommandCompletion = null;
        this.completionId = -1;
    }
}