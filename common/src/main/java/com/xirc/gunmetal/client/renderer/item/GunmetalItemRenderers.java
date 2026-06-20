package com.xirc.gunmetal.client.renderer.item;

import com.xirc.gunmetal.registry.GunmetalItems;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;

public final class GunmetalItemRenderers {
    private GunmetalItemRenderers() {
    }

    public static void register() {
        AzItemRendererRegistry.register(GunmetalItems.BERETTA.get(), BerettaItemRenderer::create);
        AzIdentityRegistry.register(GunmetalItems.BERETTA.get());
    }
}
