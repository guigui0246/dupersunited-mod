package wtf.dupers.dupersunited.mixin.entity;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.modules.render.EspModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract UUID getUuid();

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void forceGlow(CallbackInfoReturnable<Boolean> cir) {
        EspModule esp = MainClient.MODULE_MANAGER.getModule(EspModule.class);
        if (!esp.isEnabled()) {
            return;
        }
        if (EspModule.selectedEntityIds.isEmpty()) {
            return;
        }

        Entity self = (Entity) (Object) this;
        if (!EspModule.selectedEntityIds.contains(self.getType())) {
            return;
        }

        if (self instanceof PlayerEntity) {
            if (this.getUuid().version() != 4) {
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(true);
            }
            return;
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "getTeamColorValue", at = {@At("RETURN")}, cancellable = true)
    public void changeColorValue(CallbackInfoReturnable<Integer> cir) {
        Module esp = MainClient.MODULE_MANAGER.getModuleByName("esp");
        if (!esp.isEnabled()) {
            return;
        }
        if (EspModule.selectedEntityIds.isEmpty()) {
            return;
        }

        Entity self = (Entity) (Object) this;
        if (!EspModule.selectedEntityIds.contains(self.getType())) {
            return;
        }

        if (self instanceof PlayerEntity) {
            cir.setReturnValue(0xFFF800F8);
        } else if (self instanceof MobEntity) {
            cir.setReturnValue(0xFFD68542);
        } else if (self instanceof ItemEntity) {
            cir.setReturnValue(0xFFFFFFFF);
        } else {
            cir.setReturnValue(0xFFF800F8);
        }
    }
}
