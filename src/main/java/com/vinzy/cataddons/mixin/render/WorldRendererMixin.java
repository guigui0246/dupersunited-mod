package com.vinzy.cataddons.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.vinzy.cataddons.MainClient;
import com.vinzy.cataddons.modules.render.BlockEspModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilderStorage;getOutlineVertexConsumers()Lnet/minecraft/client/render/OutlineVertexConsumerProvider;"))
    private void renderBlockEsp(GpuBufferSlice gpuBufferSlice, WorldRenderState renderStates, Profiler profiler, Matrix4f matrix4f, Handle<Framebuffer> handle, Handle<Framebuffer> handle2, boolean bl, Frustum frustum, Handle<Framebuffer> handle3, Handle<Framebuffer> handle4, CallbackInfo ci, @Local MatrixStack matrices) {
        BlockEspModule esp = MainClient.MODULE_MANAGER.getModule(BlockEspModule.class);
        if (esp == null || !esp.isEnabled() || BlockEspModule.selectedBlocks.isEmpty()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d camPos = renderStates.cameraRenderState.pos;
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        esp.onRender(matrices, immediate, camPos);
        immediate.draw(BlockEspModule.ESP_LINES);
    }
}