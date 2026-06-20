package com.xirc.gunmetal.forge.client;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.client.GunmetalClient;
import com.xirc.gunmetal.client.renderer.entity.BulletRenderer;
import com.xirc.gunmetal.registry.GunmetalEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Gunmetal.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GunmetalForgeClient {
    private GunmetalForgeClient() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GunmetalEntityTypes.BULLET.get(), BulletRenderer::new);
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(GunmetalClient::init);
    }
}
