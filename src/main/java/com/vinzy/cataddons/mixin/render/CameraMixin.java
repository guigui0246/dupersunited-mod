package com.vinzy.cataddons.mixin.render;

import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.FreeLookModule;
import com.vinzy.cataddons.modules.render.FreecamModule;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Vec3d pos;
    @Shadow private float yaw;
    @Shadow private float pitch;
    @Final @Shadow private Quaternionf rotation;

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        FreecamModule freecam = MainClient.MODULE_MANAGER.getModule(FreecamModule.class);
        if (freecam != null && freecam.isEnabled() && focusedEntity != null) {
            float lerpedYaw   = (float) freecam.getLerpedYaw(tickDelta);
            float lerpedPitch = (float) freecam.getLerpedPitch(tickDelta);

            this.pos   = new Vec3d(freecam.getLerpedX(tickDelta), freecam.getLerpedY(tickDelta), freecam.getLerpedZ(tickDelta));
            this.yaw   = lerpedYaw;
            this.pitch = lerpedPitch;

            this.rotation.identity()
                    .rotateY((float) Math.toRadians(180f - lerpedYaw))
                    .rotateX((float) Math.toRadians(lerpedPitch));
            return;
        }

        if (focusedEntity == null || !thirdPerson) return;

        FreeLookModule freeLook = MainClient.MODULE_MANAGER.getModule(FreeLookModule.class);
        if (freeLook == null || !freeLook.isEnabled()) return;

        double renderX = focusedEntity.getLerpedPos(tickDelta).x;
        double renderY = focusedEntity.getLerpedPos(tickDelta).y + focusedEntity.getStandingEyeHeight();
        double renderZ = focusedEntity.getLerpedPos(tickDelta).z;

        double distance = 4.0;
        double radYaw   = Math.toRadians(freeLook.smoothYaw);

        double x = renderX - Math.sin(radYaw) * distance;
        double z = renderZ + Math.cos(radYaw) * distance;

        this.pos = new Vec3d(x, renderY, z);

        double dx = renderX - x;
        double dy = 0;
        double dz = renderZ - z;

        float facingYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float facingPitch = -(float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        this.yaw   = facingYaw;
        this.pitch = facingPitch;

        this.rotation.identity()
                .rotateY((float) Math.toRadians(180f - facingYaw))
                .rotateX((float) Math.toRadians(facingPitch));
    }
}