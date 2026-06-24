package com.xirc.gunmetal.datagen;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.datagen.providers.GunStatsProvider;
import com.xirc.gunmetal.datagen.providers.assets.GunmetalModelProvider;
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
        pack.addProvider(GunmetalModelProvider::new);
        pack.addProvider(GunStatsProvider::new);
    }
}
