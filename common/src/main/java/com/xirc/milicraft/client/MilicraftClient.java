package com.xirc.milicraft.client;

import com.xirc.milicraft.client.animation.MilicraftAnimations;
import com.xirc.milicraft.client.gui.AmmoBoxScreen;
import com.xirc.milicraft.client.gui.GunBenchScreen;
import com.xirc.milicraft.client.hud.GunHud;
import com.xirc.milicraft.client.input.MilicraftKeyMappings;
import com.xirc.milicraft.client.renderer.item.MilicraftItemRenderers;
import com.xirc.milicraft.registry.MilicraftMenus;
import dev.architectury.registry.menu.MenuRegistry;

public final class MilicraftClient {
    private MilicraftClient() {
    }

    public static void init() {
        init(true);
    }

    public static void init(boolean registerKeyMappings) {
        if (registerKeyMappings) {
            MilicraftKeyMappings.init();
        } else {
            MilicraftKeyMappings.initTickEvents();
        }
        MilicraftItemRenderers.register();
        MilicraftAnimations.init();
        GunDataSyncClient.init();
        GunHud.init();
        MenuRegistry.registerScreenFactory(MilicraftMenus.AMMO_BOX.get(), AmmoBoxScreen::new);
        MenuRegistry.registerScreenFactory(MilicraftMenus.GUN_BENCH.get(), GunBenchScreen::new);
    }
}
