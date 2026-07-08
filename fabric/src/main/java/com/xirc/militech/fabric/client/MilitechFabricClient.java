package com.xirc.militech.fabric.client;

import com.xirc.militech.client.MilitechClient;
import com.xirc.militech.client.renderer.entity.BulletRenderer;
import com.xirc.militech.registry.MilitechEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class MilitechFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MilitechClient.init();
        EntityRendererRegistry.register(MilitechEntityTypes.BULLET.get(), BulletRenderer::new);
    }
}
