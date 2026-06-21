package com.xirc.gunmetal.client;

import com.xirc.gunmetal.client.animation.GunmetalAnimations;
import com.xirc.gunmetal.client.input.GunmetalKeyMappings;
import com.xirc.gunmetal.client.renderer.item.GunmetalItemRenderers;

public final class GunmetalClient {
    private GunmetalClient() {
    }

    public static void init() {
        GunmetalKeyMappings.init();
        GunmetalItemRenderers.register();
        GunmetalAnimations.init();
    }
}
