package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import com.xirc.gunmetal.registry.GunmetalSoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BerettaItem extends AbstractGunItem {
    public BerettaItem(Properties settings) {
        super(settings);
    }

    @Override
    protected AmmoType ammoType() {
        return AmmoType.PISTOL;
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
    protected List<ReloadPart> reloadParts(ItemStack stack) {
        if (getShots(stack) <= 0) {
            // "reload": no round chambered. Parts -> draw mag, insert mag, rack slide.
            return List.of(
                    new ReloadPart("reload_start", 7),
                    new ReloadPart("reload_load", 7),
                    new ReloadPart("reload_end", 5));
        }
        // "deload": a round is still chambered, so no slide rack at the end.
        return List.of(
                new ReloadPart("deload_start", 8),
                new ReloadPart("deload_load", 7),
                new ReloadPart("deload_end", 6));
    }
}
