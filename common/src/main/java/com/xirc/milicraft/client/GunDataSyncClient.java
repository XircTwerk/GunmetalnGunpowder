package com.xirc.milicraft.client;

import com.xirc.milicraft.common.data.gun.GunAssemblyManager;
import com.xirc.milicraft.common.data.gun.GunAssemblyRecipe;
import com.xirc.milicraft.common.data.gun.GunDataSync;
import com.xirc.milicraft.common.data.gun.GunStats;
import com.xirc.milicraft.common.data.gun.GunStatsManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Client-side receiver for {@link GunDataSync}; applies synced gun data locally. */
public final class GunDataSyncClient {
    private GunDataSyncClient() {
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, GunDataSync.SYNC_GUN_DATA, (buf, context) -> {
            Map<ResourceLocation, GunStats> stats = new HashMap<>();
            int statCount = buf.readVarInt();
            for (int i = 0; i < statCount; i++) {
                ResourceLocation id = buf.readResourceLocation();
                stats.put(id, GunStats.fromJson(GsonHelper.parse(buf.readUtf())));
            }
            List<GunAssemblyRecipe> recipes = new ArrayList<>();
            int recipeCount = buf.readVarInt();
            for (int i = 0; i < recipeCount; i++) {
                recipes.add(GunAssemblyRecipe.read(buf));
            }
            context.queue(() -> {
                GunStatsManager.setStats(stats);
                GunAssemblyManager.setRecipes(recipes);
            });
        });
    }
}
