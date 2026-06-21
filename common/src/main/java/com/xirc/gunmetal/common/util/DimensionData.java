package com.xirc.gunmetal.common.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DimensionData {
    private final LivingEntity user;
    private @Nullable Vec3 pos;
    private final ResourceKey<Level> worldKey;
    private final InteractionHand hand;
    private int timer = 300;

    public DimensionData(LivingEntity user, ResourceKey<Level> worldKey, int timer) {
        this(user, worldKey, timer, InteractionHand.MAIN_HAND);
    }

    public DimensionData(LivingEntity user, ResourceKey<Level> worldKey, int timer, InteractionHand hand) {
        this.user = user;
        this.worldKey = worldKey;
        this.hand = hand;
        this.timer = timer;
    }

    public DimensionData(LivingEntity user, @Nullable Vec3 pos, ResourceKey<Level> worldKey) {
        this.user = user;
        this.pos = pos;
        this.worldKey = worldKey;
        this.hand = InteractionHand.MAIN_HAND;
    }

    public DimensionData(LivingEntity user, @Nullable Vec3 pos, ResourceKey<Level> worldKey, int timer) {
        this.user = user;
        this.pos = pos;
        this.worldKey = worldKey;
        this.hand = InteractionHand.MAIN_HAND;
        if (timer <= 0) {
            throw new IllegalArgumentException("timer must be positive!");
        }
        this.timer = timer;
    }

    public LivingEntity getUser() {
        return user;
    }

    public @Nullable Vec3 getPos() {
        return pos;
    }

    public ResourceKey<Level> getWorldKey() {
        return worldKey;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public int getTimer() {
        return timer;
    }

    public void decreaseTimer() {
        timer--;
    }
}
