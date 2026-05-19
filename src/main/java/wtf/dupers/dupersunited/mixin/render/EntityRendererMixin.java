package wtf.dupers.dupersunited.mixin.render;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.render.HidePlayersModule;
import wtf.dupers.dupersunited.modules.render.NoRenderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Unique
    private boolean shouldRenderPlayer(Entity entity) {
        HidePlayersModule mod = MainClient.MODULE_MANAGER.getModule(HidePlayersModule.class);
        if (!mod.isEnabled() || !(entity instanceof PlayerEntity) || entity.getUuid().version() != 4 || entity == MinecraftClient.getInstance().player) return true;
        if (mod.hideAll.getValue()) return false;
        if (MinecraftClient.getInstance().player == null) return true;
        return entity.squaredDistanceTo(MinecraftClient.getInstance().player) > (mod.distance.getValue() * mod.distance.getValue());
    }

    @Unique
    private boolean noRenderHides(Entity entity) {
        NoRenderModule mod = MainClient.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (mod == null || !mod.isEnabled() || NoRenderModule.selectedEntityIds.isEmpty()) return false;
        if (entity == MinecraftClient.getInstance().player) return false;
        return NoRenderModule.selectedEntityIds.contains(entity.getType());
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!shouldRenderPlayer(entity)) {
            cir.setReturnValue(false);
            return;
        }
        if (noRenderHides(entity)) {
            cir.setReturnValue(false);
        }
    }

}
