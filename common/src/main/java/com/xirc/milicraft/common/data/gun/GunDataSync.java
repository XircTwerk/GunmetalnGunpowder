package com.xirc.milicraft.common.data.gun;

import com.xirc.milicraft.Milicraft;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Server-to-client sync of datapack gun data (gun stats and assembly recipes).
 * Sent to each player on join and rebroadcast whenever the server reloads its
 * datapacks, so dedicated-server clients see real values instead of code defaults.
 */
public final class GunDataSync {
    public static final ResourceLocation SYNC_GUN_DATA = Milicraft.id("sync_gun_data");

    @Nullable
    private static MinecraftServer server;

    private GunDataSync() {
    }

    public static void setServer(@Nullable MinecraftServer runningServer) {
        server = runningServer;
    }

    public static void syncTo(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, SYNC_GUN_DATA, write(new FriendlyByteBuf(Unpooled.buffer())));
    }

    public static void broadcast() {
        MinecraftServer running = server;
        if (running == null) {
            return;
        }
        for (ServerPlayer player : running.getPlayerList().getPlayers()) {
            syncTo(player);
        }
    }

    private static FriendlyByteBuf write(FriendlyByteBuf buf) {
        Map<ResourceLocation, GunStats> stats = GunStatsManager.snapshot();
        buf.writeVarInt(stats.size());
        stats.forEach((id, gunStats) -> {
            buf.writeResourceLocation(id);
            buf.writeUtf(gunStats.toJson().toString());
        });

        List<GunAssemblyRecipe> recipes = GunAssemblyManager.recipes();
        buf.writeVarInt(recipes.size());
        for (GunAssemblyRecipe recipe : recipes) {
            recipe.write(buf);
        }
        return buf;
    }
}
