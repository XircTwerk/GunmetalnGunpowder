package com.xirc.militech.client.renderer.item;

import com.xirc.militech.registry.MilitechGuns;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;

public final class MilitechItemRenderers {
    private MilitechItemRenderers() {
    }

    public static void register() {
        AzItemRendererRegistry.register(MilitechGuns.BERETTA.get(), BerettaItemRenderer::create);
        AzIdentityRegistry.register(MilitechGuns.BERETTA.get());
        AzItemRendererRegistry.register(MilitechGuns.ASSAULT_RIFLE.get(), AssaultRifleItemRenderer::create);
        AzIdentityRegistry.register(MilitechGuns.ASSAULT_RIFLE.get());
    }
}
