package com.xirc.militech.registry;

import net.minecraft.world.level.GameRules;

public interface MilitechGameRules {
    GameRules.Key<GameRules.BooleanValue> BULLETS_BREAK_BLOCKS = GameRules.register(
            "bulletsChipBlocks",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(true));

    static void init() {
    }
}
