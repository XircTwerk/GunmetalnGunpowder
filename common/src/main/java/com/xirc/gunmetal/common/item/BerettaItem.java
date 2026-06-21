package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import net.minecraft.resources.ResourceLocation;

public class BerettaItem extends AbstractGunItem {
    public BerettaItem(Properties settings) {
        super(settings);
    }

    @Override
    protected ResourceLocation statsId() {
        return GunStatsDefaults.BERETTA_ID;
    }

    @Override
    protected GunStats defaultStats() {
        return GunStatsDefaults.BERETTA;
    }
}
