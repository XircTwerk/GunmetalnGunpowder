package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.common.system.GunAiming;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MilitechPacketRegistry {
    ResourceLocation GUN_INPUT = Militech.id("gun_input");

    static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, GUN_INPUT, (buf, context) -> {
            GunInput input = buf.readEnum(GunInput.class);
            Vec3 muzzle = buf.readBoolean()
                    ? new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
                    : null;
            if (context.getPlayer() instanceof ServerPlayer player) {
                context.queue(() -> handleGunInput(player, input, muzzle));
            }
        });
    }

    static void handleGunInput(ServerPlayer player, GunInput input, @Nullable Vec3 muzzle) {
        switch (input) {
            case SHOOT -> shootPreferredGun(player, muzzle);
            case SHOOT_OFFHAND -> shootOffhandGun(player, muzzle);
            case RELOAD -> reloadPreferredGun(player);
            case AIM_START -> {
                if (player.getMainHandItem().getItem() instanceof AbstractGunItem
                        && !(player.getOffhandItem().getItem() instanceof AbstractGunItem)) {
                    GunAiming.set(player, true);
                }
            }
            case AIM_STOP -> GunAiming.set(player, false);
        }
    }

    static void shootPreferredGun(ServerPlayer player, @Nullable Vec3 muzzle) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AbstractGunItem gun) {
            gun.tryShoot(player, mainHand, muzzle);
        }
    }

    static void shootOffhandGun(ServerPlayer player, @Nullable Vec3 muzzle) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof AbstractGunItem gun) {
            gun.tryShoot(player, offhand, muzzle);
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

    static FriendlyByteBuf write(GunInput input, @Nullable Vec3 muzzle, FriendlyByteBuf buf) {
        buf.writeEnum(input);
        buf.writeBoolean(muzzle != null);
        if (muzzle != null) {
            buf.writeDouble(muzzle.x);
            buf.writeDouble(muzzle.y);
            buf.writeDouble(muzzle.z);
        }
        return buf;
    }

    enum GunInput {
        SHOOT,
        SHOOT_OFFHAND,
        RELOAD,
        AIM_START,
        AIM_STOP
    }
}
