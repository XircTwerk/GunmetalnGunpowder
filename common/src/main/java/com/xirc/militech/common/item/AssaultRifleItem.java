package com.xirc.militech.common.item;

import com.xirc.militech.common.data.gun.GunStats;
import com.xirc.militech.common.data.gun.GunStatsDefaults;
import com.xirc.militech.registry.MilitechSoundRegistry;
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
        return MilitechSoundRegistry.ASSAULT_RIFLE_SHOT.get();
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
