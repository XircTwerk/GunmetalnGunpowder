package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import com.xirc.gunmetal.registry.GunmetalSoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class AssaultRifleItem extends AbstractGunItem {
    public AssaultRifleItem(Properties settings) {
        super(settings);
    }

    @Override
    protected AmmoType ammoType() {
        return AmmoType.RIFLE;
    }

    @Override
    protected ResourceLocation statsId() {
        return GunStatsDefaults.ASSAULT_RIFLE_ID;
    }

    @Override
    protected GunStats defaultStats() {
        return GunStatsDefaults.ASSAULT_RIFLE;
    }

    @Override
    protected SoundEvent fireSound() {
        return GunmetalSoundRegistry.ASSAULT_RIFLE_SHOT.get();
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
