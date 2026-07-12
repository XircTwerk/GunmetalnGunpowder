package com.xirc.militech.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class KnifeItem extends AbstractMeleeItem {
    private static final String COMBO_INDEX_ID = "MilitechKnifeCombo";
    private static final String COMBO_AT_ID = "MilitechKnifeComboAt";

    // Small reach with a tight hit radius.
    private static final double REACH = 3.0;
    private static final double HIT_RADIUS = 0.0;

    private static final float SLASH_DAMAGE = 4.0f;
    private static final int SLASH_COOLDOWN_TICKS = 8;
    private static final int COMBO_RESET_TICKS = 20;

    private static final float HEAVY_DAMAGE = 7.0f;
    private static final int HEAVY_COOLDOWN_TICKS = 20;

    public KnifeItem(Properties settings) {
        super(settings);
    }

    @Override
    public String playerAnimationFile() {
        return "knife";
    }

    @Override
    protected void primaryAttack(ServerPlayer player, ItemStack stack) {
        String animation = nextComboAnimation(player, stack);
        markAnimation(stack, animation);
        performMeleeHit(player, stack, SLASH_DAMAGE, REACH, HIT_RADIUS);
        addCooldown(player, stack, SLASH_COOLDOWN_TICKS);
    }

    @Override
    protected void secondaryAttack(ServerPlayer player, ItemStack stack) {
        markAnimation(stack, "heavy_stab");
        performMeleeHit(player, stack, HEAVY_DAMAGE, REACH, HIT_RADIUS);
        addCooldown(player, stack, HEAVY_COOLDOWN_TICKS);
    }

    /** Alternates slash_1 / slash_2, resetting to slash_1 after a pause between swings. */
    private String nextComboAnimation(ServerPlayer player, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag();
        long now = player.level().getGameTime();
        int index = data.getInt(COMBO_INDEX_ID);
        if (now - data.getLong(COMBO_AT_ID) > COMBO_RESET_TICKS) {
            index = 0;
        }
        data.putInt(COMBO_INDEX_ID, index == 0 ? 1 : 0);
        data.putLong(COMBO_AT_ID, now);
        return index == 0 ? "slash_1" : "slash_2";
    }
}
