package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface GunmetalPacketRegistry {
    ResourceLocation GUN_INPUT = Gunmetal.id("gun_input");

    static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, GUN_INPUT, (buf, context) -> {
            GunInput input = buf.readEnum(GunInput.class);
            if (context.getPlayer() instanceof ServerPlayer player) {
                context.queue(() -> handleGunInput(player, input));
            }
        });
    }

    static void handleGunInput(ServerPlayer player, GunInput input) {
        switch (input) {
            case SHOOT -> shootPreferredGun(player);
            case SHOOT_OFFHAND -> shootOffhandGun(player);
            case RELOAD -> reloadPreferredGun(player);
        }
    }

    static void shootPreferredGun(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AbstractGunItem gun) {
            gun.tryShoot(player, mainHand);
        }
    }

    static void shootOffhandGun(ServerPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof AbstractGunItem gun) {
            gun.tryShoot(player, offhand);
        }
    }

    static void reloadPreferredGun(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AbstractGunItem gun) {
            gun.tryReload(player, mainHand);
            return;
        }

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof AbstractGunItem gun) {
            gun.tryReload(player, offhand);
        }
    }

    static FriendlyByteBuf write(GunInput input, FriendlyByteBuf buf) {
        buf.writeEnum(input);
        return buf;
    }

    enum GunInput {
        SHOOT,
        SHOOT_OFFHAND,
        RELOAD
    }
}
