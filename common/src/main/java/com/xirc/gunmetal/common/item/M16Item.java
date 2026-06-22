package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import com.xirc.gunmetal.registry.GunmetalSoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class M16Item extends AbstractGunItem {
    public M16Item(Properties settings) {
        super(settings);
    }

    @Override
    protected ResourceLocation statsId() {
        return GunStatsDefaults.M16_ID;
    }

    @Override
    protected GunStats defaultStats() {
        return GunStatsDefaults.M16;
    }

    @Override
    protected SoundEvent fireSound() {
        return GunmetalSoundRegistry.M16_SHOT.get();
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
