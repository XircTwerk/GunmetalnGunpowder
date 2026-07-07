package com.xirc.milicraft.registry;

import net.minecraft.world.level.GameRules;

public interface MilicraftGameRules {
    GameRules.Key<GameRules.BooleanValue> BULLETS_BREAK_BLOCKS = GameRules.register(
            "bulletsChipBlocks",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(true));

    static void init() {
    }
}
