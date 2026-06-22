package com.xirc.gunmetal.client.renderer.item;

import com.xirc.gunmetal.registry.GunmetalGuns;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;

public final class GunmetalItemRenderers {
    private GunmetalItemRenderers() {
    }

    public static void register() {
        AzItemRendererRegistry.register(GunmetalGuns.BERETTA.get(), BerettaItemRenderer::create);
        AzIdentityRegistry.register(GunmetalGuns.BERETTA.get());
        AzItemRendererRegistry.register(GunmetalGuns.M16.get(), M16ItemRenderer::create);
        AzIdentityRegistry.register(GunmetalGuns.M16.get());
    }
}
