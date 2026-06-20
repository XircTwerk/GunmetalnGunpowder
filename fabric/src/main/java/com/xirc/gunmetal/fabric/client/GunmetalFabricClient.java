package com.xirc.gunmetal.fabric.client;

import com.xirc.gunmetal.client.GunmetalClient;
import com.xirc.gunmetal.client.renderer.entity.BulletRenderer;
import com.xirc.gunmetal.registry.GunmetalEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class GunmetalFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GunmetalClient.init();
        EntityRendererRegistry.register(GunmetalEntityTypes.BULLET.get(), BulletRenderer::new);
    }
}
