package com.xirc.milicraft;

import com.xirc.milicraft.common.data.gun.GunStatsManager;
import com.xirc.milicraft.common.event.MilicraftEvents;
import dev.architectury.registry.ReloadListenerRegistry;
import com.xirc.milicraft.registry.MilicraftCreativeTabs;
import com.xirc.milicraft.registry.MilicraftEntityTypes;
import com.xirc.milicraft.registry.MilicraftGameRules;
import com.xirc.milicraft.registry.MilicraftGuns;
import com.xirc.milicraft.registry.MilicraftItems;
import com.xirc.milicraft.registry.MilicraftMenus;
import com.xirc.milicraft.registry.MilicraftPacketRegistry;
import com.xirc.milicraft.registry.MilicraftSoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Milicraft {
    public static final String MOD_ID = "milicraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        MilicraftGameRules.init();
        MilicraftItems.init();
        MilicraftGuns.init();
        MilicraftEntityTypes.init();
        MilicraftSoundRegistry.init();
        MilicraftCreativeTabs.init();
        MilicraftMenus.init();
        MilicraftPacketRegistry.init();
        MilicraftEvents.init();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, GunStatsManager.INSTANCE, id("gun_stats"));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
