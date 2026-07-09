package com.xirc.militech.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.xirc.militech.mixin_logic.MilitechDepthHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Exposes RenderTarget.depthBufferId through MilitechDepthHolder so post-process shaders
 * can bind MC's existing depth texture as DiffuseDepthSampler without reflection or
 * access wideners.
 */
@Mixin(RenderTarget.class)
public class RenderTargetDepthMixin implements MilitechDepthHolder {

    @Shadow
    protected int depthBufferId;

    @Override
    public int militech$getDepthTexId() {
        return depthBufferId;
    }
}
