package com.xirc.militech.common.item;

import net.minecraft.world.item.Item;

/**
 * A gun component consumed by the gun bench. The part type decides which
 * assembly slot accepts it.
 */
public class GunPartItem extends Item {
    private final GunPartType partType;

    public GunPartItem(Properties settings, GunPartType partType) {
        super(settings);
        this.partType = partType;
    }

    public GunPartType getPartType() {
        return partType;
    }
}
