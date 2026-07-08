package com.xirc.militech.common.item;

import net.minecraft.world.item.Item;

/**
 * A single round of a specific {@link AmmoType}.
 */
public class AmmoItem extends Item {
    private final AmmoType ammoType;

    public AmmoItem(Properties settings, AmmoType ammoType) {
        super(settings);
        this.ammoType = ammoType;
    }

    public AmmoType getAmmoType() {
        return ammoType;
    }
}
