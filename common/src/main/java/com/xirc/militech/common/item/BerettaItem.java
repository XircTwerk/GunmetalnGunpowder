package com.xirc.militech.common.item;

import com.xirc.militech.common.data.gun.GunStats;
import com.xirc.militech.common.data.gun.GunStatsDefaults;
import com.xirc.militech.registry.MilitechSoundRegistry;
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
        return MilitechSoundRegistry.BERETTA_M9_SHOT.get();
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

    @Override
    protected SoundEvent reloadPartSound(String part) {
        return switch (part) {
            case "reload_start", "deload_start" -> MilitechSoundRegistry.WEAP_BOLT_OUT.get();
            case "reload_load", "deload_load" -> MilitechSoundRegistry.WEAP_MAGIN_PLASTIC.get();
            case "reload_end", "deload_end" -> MilitechSoundRegistry.WEAP_BOLT_OUT.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent reloadPartEndSound(String part) {
        return "reload_start".equals(part) || "deload_start".equals(part)
                ? MilitechSoundRegistry.WEAP_MAGDROP_PLASTIC.get()
                : null;
    }

    @Override
    protected SoundEvent reloadCompleteSound() {
        return null;
    }
}
