package com.xirc.gunmetal.client;

import com.xirc.gunmetal.client.animation.GunmetalAnimations;
import com.xirc.gunmetal.client.input.GunmetalKeyMappings;
import com.xirc.gunmetal.client.renderer.item.GunmetalItemRenderers;

public final class GunmetalClient {
    private GunmetalClient() {
    }

    public static void init() {
        init(true);
    }

    public static void init(boolean registerKeyMappings) {
        if (registerKeyMappings) {
            GunmetalKeyMappings.init();
        } else {
            GunmetalKeyMappings.initTickEvents();
        }
        GunmetalItemRenderers.register();
        GunmetalAnimations.init();
    }
}
