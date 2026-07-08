package com.xirc.militech.client;

import com.xirc.militech.client.animation.MilitechAnimations;
import com.xirc.militech.client.gui.AmmoBoxScreen;
import com.xirc.militech.client.gui.GunBenchScreen;
import com.xirc.militech.client.hud.GunHud;
import com.xirc.militech.client.input.MilitechKeyMappings;
import com.xirc.militech.client.renderer.item.MilitechItemRenderers;
import com.xirc.militech.registry.MilitechMenus;
import dev.architectury.registry.menu.MenuRegistry;

public final class MilitechClient {
    private MilitechClient() {
    }

    public static void init() {
        init(true);
    }

    public static void init(boolean registerKeyMappings) {
        if (registerKeyMappings) {
            MilitechKeyMappings.init();
        } else {
            MilitechKeyMappings.initTickEvents();
        }
        MilitechItemRenderers.register();
        MilitechAnimations.init();
        GunDataSyncClient.init();
        GunHud.init();
        MenuRegistry.registerScreenFactory(MilitechMenus.AMMO_BOX.get(), AmmoBoxScreen::new);
        MenuRegistry.registerScreenFactory(MilitechMenus.GUN_BENCH.get(), GunBenchScreen::new);
    }
}
