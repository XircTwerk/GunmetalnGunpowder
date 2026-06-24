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
        AzItemRendererRegistry.register(GunmetalGuns.ASSAULT_RIFLE.get(), AssaultRifleItemRenderer::create);
        AzIdentityRegistry.register(GunmetalGuns.ASSAULT_RIFLE.get());
    }
}
