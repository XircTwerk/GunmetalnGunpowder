package com.xirc.gunmetal.datagen;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.datagen.providers.GunStatsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class GunmetalDataGen implements DataGeneratorEntrypoint {
    @Override
    public String getEffectiveModId() {
        return Gunmetal.MOD_ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(GunStatsProvider::new);
    }
}
