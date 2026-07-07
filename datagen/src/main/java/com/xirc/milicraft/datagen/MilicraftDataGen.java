package com.xirc.milicraft.datagen;

import com.xirc.milicraft.Milicraft;
import com.xirc.milicraft.datagen.providers.GunStatsProvider;
import com.xirc.milicraft.datagen.providers.assets.MilicraftModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class MilicraftDataGen implements DataGeneratorEntrypoint {
    @Override
    public String getEffectiveModId() {
        return Milicraft.MOD_ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(MilicraftModelProvider::new);
        pack.addProvider(GunStatsProvider::new);
    }
}
