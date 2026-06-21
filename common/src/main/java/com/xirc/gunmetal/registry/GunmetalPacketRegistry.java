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
        ItemStack stack = player.getMainHandItem();
        AbstractGunItem gun;
        if (stack.getItem() instanceof AbstractGunItem mainHandGun) {
            gun = mainHandGun;
        } else {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof AbstractGunItem offhandGun)) {
                return;
            }
            gun = offhandGun;
        }

        switch (input) {
            case SHOOT -> gun.tryShoot(player, stack);
            case RELOAD -> gun.tryReload(player, stack);
        }
    }

    static FriendlyByteBuf write(GunInput input, FriendlyByteBuf buf) {
        buf.writeEnum(input);
        return buf;
    }

    enum GunInput {
        SHOOT,
        RELOAD
    }
}
