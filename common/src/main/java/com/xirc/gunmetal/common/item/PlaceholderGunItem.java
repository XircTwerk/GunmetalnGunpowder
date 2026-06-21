package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import net.minecraft.resources.ResourceLocation;

public class PlaceholderGunItem extends AbstractGunItem {
    public PlaceholderGunItem(Properties settings) {
        super(settings);
    }

    @Override
    protected ResourceLocation statsId() {
        return GunStatsDefaults.PLACEHOLDER_GUN_ID;
    }

    @Override
    protected GunStats defaultStats() {
        return GunStatsDefaults.PLACEHOLDER_GUN;
    }
}
