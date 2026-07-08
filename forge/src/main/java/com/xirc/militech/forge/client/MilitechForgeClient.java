package com.xirc.militech.forge.client;

import com.xirc.militech.Militech;
import com.xirc.militech.client.MilitechClient;
import com.xirc.militech.client.input.MilitechKeyMappings;
import com.xirc.militech.client.renderer.entity.BulletRenderer;
import com.xirc.militech.registry.MilitechEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Militech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MilitechForgeClient {
    private MilitechForgeClient() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MilitechEntityTypes.BULLET.get(), BulletRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MilitechKeyMappings.RELOAD);
        event.register(MilitechKeyMappings.INTERACT);
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MilitechClient.init(false));
    }
}
