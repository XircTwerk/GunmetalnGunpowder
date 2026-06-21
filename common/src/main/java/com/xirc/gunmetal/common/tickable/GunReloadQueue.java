package com.xirc.gunmetal.common.tickable;

import com.xirc.gunmetal.common.item.AbstractGunItem;
import com.xirc.gunmetal.common.util.DimensionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GunReloadQueue {
    private static final List<DimensionData> RELOADS = new ArrayList<>();
    private static final List<DimensionData> PENDING_RELOADS = new ArrayList<>();

    private GunReloadQueue() {
    }

    public static void enqueue(DimensionData dimensionData) {
        PENDING_RELOADS.add(dimensionData);
    }

    public static void remove(DimensionData dimensionData) {
        RELOADS.remove(dimensionData);
    }

    public static void tick(MinecraftServer server) {
        if (!PENDING_RELOADS.isEmpty()) {
            RELOADS.addAll(PENDING_RELOADS);
            PENDING_RELOADS.clear();
        }

        List<DimensionData> activeReloads = new ArrayList<>();

        for (DimensionData reload : RELOADS) {
            LivingEntity user = reload.getUser();
            if (user == null || !user.isAlive()) {
                continue;
            }

            int timer = reload.getTimer();
            if (timer > 0) {
                reload.decreaseTimer();
                activeReloads.add(reload);
                continue;
            }

            ServerLevel world = server.getLevel(reload.getWorldKey());
            if (world == null) {
                continue;
            }

            ItemStack stack = user.getItemInHand(reload.getHand());
            if (stack.getItem() instanceof AbstractGunItem gun) {
                gun.finishReload(stack, world, user);
            }
        }

        RELOADS.clear();
        RELOADS.addAll(activeReloads);
    }
}
