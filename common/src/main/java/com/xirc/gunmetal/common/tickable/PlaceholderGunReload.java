package com.xirc.gunmetal.common.tickable;

import com.xirc.gunmetal.common.item.AbstractGunItem;
import com.xirc.gunmetal.common.util.DimensionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class PlaceholderGunReload {
    private static final List<DimensionData> TO_RELOAD = new ArrayList<>();
    private static final List<DimensionData> TO_ADD = new ArrayList<>();

    private PlaceholderGunReload() {
    }

    public static void enqueue(DimensionData dimensionData) {
        TO_ADD.add(dimensionData);
    }

    public static void remove(DimensionData dimensionData) {
        TO_RELOAD.remove(dimensionData);
    }

    public static void tick(MinecraftServer server) {
        if (!TO_ADD.isEmpty()) {
            TO_RELOAD.addAll(TO_ADD);
            TO_ADD.clear();
        }

        List<DimensionData> newToReload = new ArrayList<>();

        for (DimensionData toReloadData : TO_RELOAD) {
            LivingEntity user = toReloadData.getUser();
            if (user != null && user.isAlive()) {
                int timer = toReloadData.getTimer();
                if (timer > 0) {
                    toReloadData.decreaseTimer();
                    newToReload.add(toReloadData);
                } else {
                    ServerLevel world = server.getLevel(toReloadData.getWorldKey());
                    if (world == null) {
                        continue;
                    }

                    ItemStack main = user.getMainHandItem();
                    if (main.getItem() instanceof AbstractGunItem gun) {
                        gun.finishReload(main, world, user);
                    }
                }
            }
        }

        TO_RELOAD.clear();
        TO_RELOAD.addAll(newToReload);
    }
}
