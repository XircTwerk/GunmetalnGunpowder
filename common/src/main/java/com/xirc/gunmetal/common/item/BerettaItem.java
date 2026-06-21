package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import com.xirc.gunmetal.registry.GunmetalSoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BerettaItem extends AbstractGunItem {
    private static final int EMPTY_RELOAD_TICKS = 17;
    private static final int TACTICAL_RELOAD_TICKS = 34;

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

    @Override
    protected SoundEvent fireSound() {
        return GunmetalSoundRegistry.BERETTA_M9_SHOT.get();
    }

    @Override
    protected String fireAnimation(ItemStack stack, Player player) {
        return !player.isCreative() && getShots(stack) == 1 ? "fire_final" : "fire";
    }

    @Override
    protected String reloadAnimation(ItemStack stack) {
        return getShots(stack) <= 0 ? "reload_empty" : "deload";
    }

    @Override
    protected int reloadDurationTicks(ItemStack stack) {
        return getShots(stack) <= 0 ? EMPTY_RELOAD_TICKS : TACTICAL_RELOAD_TICKS;
    }
}
