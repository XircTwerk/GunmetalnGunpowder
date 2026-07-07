package com.xirc.milicraft.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.xirc.milicraft.Milicraft;

@Mod(Milicraft.MOD_ID)
public final class MilicraftForge {
    public MilicraftForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Milicraft.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Milicraft.init();
    }
}
