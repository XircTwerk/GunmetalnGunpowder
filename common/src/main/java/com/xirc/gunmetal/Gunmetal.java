package com.xirc.gunmetal;

import com.xirc.gunmetal.common.event.GunmetalEvents;
import com.xirc.gunmetal.registry.GunmetalEntityTypes;
import com.xirc.gunmetal.registry.GunmetalItems;
import com.xirc.gunmetal.registry.GunmetalSoundEvents;
import net.minecraft.resources.ResourceLocation;

public final class Gunmetal {
    public static final String MOD_ID = "gunmetal";

    public static void init() {
        GunmetalItems.init();
        GunmetalEntityTypes.init();
        GunmetalSoundEvents.init();
        GunmetalEvents.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
