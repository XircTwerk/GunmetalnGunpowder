package com.xirc.gunmetal.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.xirc.gunmetal.Gunmetal;

@Mod(Gunmetal.MOD_ID)
public final class GunmetalForge {
    public GunmetalForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Gunmetal.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Gunmetal.init();
    }
}
