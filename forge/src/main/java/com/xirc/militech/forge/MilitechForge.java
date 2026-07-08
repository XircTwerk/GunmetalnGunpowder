package com.xirc.militech.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.xirc.militech.Militech;

@Mod(Militech.MOD_ID)
public final class MilitechForge {
    public MilitechForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Militech.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Militech.init();
    }
}
