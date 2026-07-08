package com.xirc.militech;

import com.xirc.militech.common.data.gun.GunAssemblyManager;
import com.xirc.militech.common.data.gun.GunStatsManager;
import com.xirc.militech.common.event.MilitechEvents;
import dev.architectury.registry.ReloadListenerRegistry;
import com.xirc.militech.registry.MilitechBlockEntities;
import com.xirc.militech.registry.MilitechBlocks;
import com.xirc.militech.registry.MilitechCreativeTabs;
import com.xirc.militech.registry.MilitechEntityTypes;
import com.xirc.militech.registry.MilitechGameRules;
import com.xirc.militech.registry.MilitechGuns;
import com.xirc.militech.registry.MilitechItems;
import com.xirc.militech.registry.MilitechMenus;
import com.xirc.militech.registry.MilitechPacketRegistry;
import com.xirc.militech.registry.MilitechSoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Militech {
    public static final String MOD_ID = "militech";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        MilitechGameRules.init();
        MilitechBlocks.init();
        MilitechBlockEntities.init();
        MilitechItems.init();
        MilitechGuns.init();
        MilitechEntityTypes.init();
        MilitechSoundRegistry.init();
        MilitechCreativeTabs.init();
        MilitechMenus.init();
        MilitechPacketRegistry.init();
        MilitechEvents.init();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, GunStatsManager.INSTANCE, id("gun_stats"));
        ReloadListenerRegistry.register(PackType.SERVER_DATA, GunAssemblyManager.INSTANCE, id("gun_assembly"));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
