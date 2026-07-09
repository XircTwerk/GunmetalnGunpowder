package com.xirc.militech.fabric.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.xirc.militech.Militech;
import com.xirc.militech.client.MilitechClient;
import com.xirc.militech.client.outline.OutlineShaderHolder;
import com.xirc.militech.client.renderer.entity.BulletRenderer;
import com.xirc.militech.registry.MilitechEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.resources.ResourceLocation;

public final class MilitechFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerOutlineShader();
        MilitechClient.init();
        EntityRendererRegistry.register(MilitechEntityTypes.BULLET.get(), BulletRenderer::new);
    }

    private static void registerOutlineShader() {
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(
                new ResourceLocation(Militech.MOD_ID, "outline_cel"),
                DefaultVertexFormat.NEW_ENTITY,
                OutlineShaderHolder::setShader));
    }
}
