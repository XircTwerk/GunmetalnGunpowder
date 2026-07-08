package com.xirc.militech.datagen;

import com.xirc.militech.Militech;
import com.xirc.militech.datagen.providers.GunAssemblyProvider;
import com.xirc.militech.datagen.providers.GunStatsProvider;
import com.xirc.militech.datagen.providers.assets.MilitechModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class MilitechDataGen implements DataGeneratorEntrypoint {
    @Override
    public String getEffectiveModId() {
        return Militech.MOD_ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(MilitechModelProvider::new);
        pack.addProvider(GunStatsProvider::new);
        pack.addProvider(GunAssemblyProvider::new);
    }
}
