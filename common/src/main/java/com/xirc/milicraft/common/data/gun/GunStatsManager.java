package com.xirc.milicraft.common.data.gun;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xirc.milicraft.Milicraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GunStatsManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, GunStats> STATS = new HashMap<>();

    public static final GunStatsManager INSTANCE = new GunStatsManager();

    private GunStatsManager() {
        super(GSON, "gun_stats");
    }

    public static Optional<GunStats> get(ResourceLocation id) {
        return Optional.ofNullable(STATS.get(id));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GunStats> loaded = new HashMap<>();
        resources.forEach((id, json) -> {
            try {
                JsonObject object = GsonHelper.convertToJsonObject(json, "gun stats");
                loaded.put(id, GunStats.fromJson(object));
            } catch (RuntimeException exception) {
                Milicraft.LOGGER.warn("Skipping invalid gun stats {}", id, exception);
            }
        });
        STATS.clear();
        STATS.putAll(loaded);
    }
}
