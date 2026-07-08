package com.xirc.militech.datagen.providers;

import com.xirc.militech.common.data.gun.GunAssemblyRecipe;
import com.xirc.militech.common.data.gun.GunAssemblyRecipes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Emits the default gun bench assembly recipes as datapack JSON, mirroring {@link GunStatsProvider}. */
public final class GunAssemblyProvider implements DataProvider {
    private final FabricDataOutput output;

    public GunAssemblyProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return CompletableFuture.allOf(GunAssemblyRecipes.RECIPES.stream()
                .map(recipe -> DataProvider.saveStable(cache, recipe.toJson(), path(recipe)))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "Gun Assembly Recipes";
    }

    private Path path(GunAssemblyRecipe recipe) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(recipe.result().get());
        return output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(id.getNamespace())
                .resolve("gun_assembly")
                .resolve(id.getPath() + ".json");
    }
}
