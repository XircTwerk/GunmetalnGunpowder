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

import java.util.ArrayList;
import java.util.List;

/**
 * Datapack loader for gun bench assembly recipes ({@code data/<ns>/gun_assembly/*.json}),
 * following the {@link GunStatsManager} pattern. When no datapack recipes are present
 * the hardcoded {@link GunAssemblyRecipes} defaults apply. Loaded recipes are synced
 * to clients so the bench works on dedicated servers.
 */
public final class GunAssemblyManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static List<GunAssemblyRecipe> loaded = List.of();

    public static final GunAssemblyManager INSTANCE = new GunAssemblyManager();

    private GunAssemblyManager() {
        super(GSON, "gun_assembly");
    }

    /** Datapack recipes if any were loaded, otherwise the code defaults. */
    public static List<GunAssemblyRecipe> recipes() {
        return loaded.isEmpty() ? GunAssemblyRecipes.RECIPES : loaded;
    }

    /** Replaces the loaded recipes; used by the client-side sync handler. */
    public static void setRecipes(List<GunAssemblyRecipe> recipes) {
        loaded = List.copyOf(recipes);
    }

    @Override
    protected void apply(java.util.Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<GunAssemblyRecipe> result = new ArrayList<>();
        resources.forEach((id, json) -> {
            try {
                JsonObject object = GsonHelper.convertToJsonObject(json, "gun assembly recipe");
                result.add(GunAssemblyRecipe.fromJson(object));
            } catch (RuntimeException exception) {
                Milicraft.LOGGER.warn("Skipping invalid gun assembly recipe {}", id, exception);
            }
        });
        loaded = List.copyOf(result);
        GunDataSync.broadcast();
    }
}
