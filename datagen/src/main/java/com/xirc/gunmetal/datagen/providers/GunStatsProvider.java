package com.xirc.gunmetal.datagen.providers;

import com.xirc.gunmetal.common.data.gun.GunStats;
import com.xirc.gunmetal.common.data.gun.GunStatsDefaults;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class GunStatsProvider implements DataProvider {
    private final FabricDataOutput output;

    public GunStatsProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        Map<ResourceLocation, GunStats> stats = new LinkedHashMap<>();
        stats.put(GunStatsDefaults.PLACEHOLDER_GUN_ID, GunStatsDefaults.PLACEHOLDER_GUN);
        stats.put(GunStatsDefaults.BERETTA_ID, GunStatsDefaults.BERETTA);

        return CompletableFuture.allOf(stats.entrySet().stream()
                .map(entry -> DataProvider.saveStable(cache, entry.getValue().toJson(), path(entry.getKey())))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "Gun Stats";
    }

    private Path path(ResourceLocation id) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(id.getNamespace())
                .resolve("gun_stats")
                .resolve(id.getPath() + ".json");
    }
}
