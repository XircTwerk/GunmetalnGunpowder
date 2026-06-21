package com.xirc.gunmetal.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import com.xirc.gunmetal.registry.GunmetalPacketRegistry;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public interface GunmetalKeyMappings {
    KeyMapping RELOAD = new KeyMapping(
            "key.gunmetal.reload",
            GLFW.GLFW_KEY_R,
            "key.categories.gunmetal"
    );
    KeyMapping INTERACT = new KeyMapping(
            "key.gunmetal.interact",
            GLFW.GLFW_KEY_I,
            "key.categories.gunmetal"
    );

    static void init() {
        KeyMappingRegistry.register(RELOAD);
        KeyMappingRegistry.register(INTERACT);
        ClientTickEvent.CLIENT_POST.register(GunmetalKeyMappings::tick);
    }

    static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (RELOAD.consumeClick()) {
            if (hasGunEquipped(minecraft)) {
                send(GunmetalPacketRegistry.GunInput.RELOAD);
            }
        }
    }

    static boolean hasGunEquipped(Minecraft minecraft) {
        return minecraft.player != null
                && (minecraft.player.getMainHandItem().getItem() instanceof AbstractGunItem
                || minecraft.player.getOffhandItem().getItem() instanceof AbstractGunItem);
    }

    static void shoot() {
        send(GunmetalPacketRegistry.GunInput.SHOOT);
    }

    static void send(GunmetalPacketRegistry.GunInput input) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        NetworkManager.sendToServer(GunmetalPacketRegistry.GUN_INPUT, GunmetalPacketRegistry.write(input, buf));
    }
}
