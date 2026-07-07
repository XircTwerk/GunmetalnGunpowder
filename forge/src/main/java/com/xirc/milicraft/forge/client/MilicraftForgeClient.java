package com.xirc.milicraft.forge.client;

import com.xirc.milicraft.Milicraft;
import com.xirc.milicraft.client.MilicraftClient;
import com.xirc.milicraft.client.input.MilicraftKeyMappings;
import com.xirc.milicraft.client.renderer.entity.BulletRenderer;
import com.xirc.milicraft.registry.MilicraftEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Milicraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MilicraftForgeClient {
    private MilicraftForgeClient() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MilicraftEntityTypes.BULLET.get(), BulletRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MilicraftKeyMappings.RELOAD);
        event.register(MilicraftKeyMappings.INTERACT);
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MilicraftClient.init(false));
    }
}
