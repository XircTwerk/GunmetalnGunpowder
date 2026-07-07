package com.xirc.milicraft.common.item;

import com.xirc.milicraft.common.data.gun.GunStats;
import com.xirc.milicraft.common.data.gun.GunStatsDefaults;
import com.xirc.milicraft.registry.MilicraftSoundRegistry;
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
        return MilicraftSoundRegistry.ASSAULT_RIFLE_SHOT.get();
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
