package com.xirc.gunmetal.client.tracer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class MuzzleTracker {
    private static final Map<UUID, Vec3> POSITIONS = new ConcurrentHashMap<>();

    private MuzzleTracker() {}

    public static void record(UUID shooterId, Vec3 worldPos) {
        POSITIONS.put(shooterId, worldPos);
    }

    public static Vec3 get(UUID shooterId) {
        return POSITIONS.get(shooterId);
    }
}
