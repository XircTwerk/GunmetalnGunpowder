package com.xirc.gunmetal.client;

import com.xirc.gunmetal.client.animation.GunmetalAnimations;
import com.xirc.gunmetal.client.gui.AmmoBoxScreen;
import com.xirc.gunmetal.client.hud.GunHud;
import com.xirc.gunmetal.client.input.GunmetalKeyMappings;
import com.xirc.gunmetal.client.renderer.item.GunmetalItemRenderers;
import com.xirc.gunmetal.registry.GunmetalMenus;
import dev.architectury.registry.menu.MenuRegistry;

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
        GunHud.init();
        MenuRegistry.registerScreenFactory(GunmetalMenus.AMMO_BOX.get(), AmmoBoxScreen::new);
    }
}
