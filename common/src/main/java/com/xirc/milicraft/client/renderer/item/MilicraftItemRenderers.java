package com.xirc.milicraft.client.renderer.item;

import com.xirc.milicraft.registry.MilicraftGuns;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;

public final class MilicraftItemRenderers {
    private MilicraftItemRenderers() {
    }

    public static void register() {
        AzItemRendererRegistry.register(MilicraftGuns.BERETTA.get(), BerettaItemRenderer::create);
        AzIdentityRegistry.register(MilicraftGuns.BERETTA.get());
        AzItemRendererRegistry.register(MilicraftGuns.ASSAULT_RIFLE.get(), AssaultRifleItemRenderer::create);
        AzIdentityRegistry.register(MilicraftGuns.ASSAULT_RIFLE.get());
    }
}
