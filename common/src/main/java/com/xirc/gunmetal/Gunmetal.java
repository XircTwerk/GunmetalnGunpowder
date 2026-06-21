package com.xirc.gunmetal;

import com.xirc.gunmetal.common.data.gun.GunStatsManager;
import com.xirc.gunmetal.common.event.GunmetalEvents;
import dev.architectury.registry.ReloadListenerRegistry;
import com.xirc.gunmetal.registry.GunmetalCreativeTabs;
import com.xirc.gunmetal.registry.GunmetalEntityTypes;
import com.xirc.gunmetal.registry.GunmetalGuns;
import com.xirc.gunmetal.registry.GunmetalItems;
import com.xirc.gunmetal.registry.GunmetalPacketRegistry;
import com.xirc.gunmetal.registry.GunmetalSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Gunmetal {
    public static final String MOD_ID = "gunmetal";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        GunmetalItems.init();
        GunmetalGuns.init();
        GunmetalEntityTypes.init();
        GunmetalSoundEvents.init();
        GunmetalCreativeTabs.init();
        GunmetalPacketRegistry.init();
        GunmetalEvents.init();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, GunStatsManager.INSTANCE, id("gun_stats"));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
