package com.xirc.milicraft.fabric.client;

import com.xirc.milicraft.client.MilicraftClient;
import com.xirc.milicraft.client.renderer.entity.BulletRenderer;
import com.xirc.milicraft.registry.MilicraftEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class MilicraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MilicraftClient.init();
        EntityRendererRegistry.register(MilicraftEntityTypes.BULLET.get(), BulletRenderer::new);
    }
}
