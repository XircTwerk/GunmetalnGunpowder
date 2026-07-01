package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface GunmetalPacketRegistry {
    ResourceLocation GUN_INPUT = Gunmetal.id("gun_input");

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
        RELOAD
    }
}
