package com.xirc.gunmetal.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xirc.gunmetal.client.tracer.TracerRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public class TracerLevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void gunmetal$renderTracers(PoseStack poseStack, float partialTick, long finishNanoTime,
                                        boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                        LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        TracerRenderer.renderAll(poseStack, camera, partialTick);
    }
}
