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
    boolean[] TICK_EVENTS_REGISTERED = {false};

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
        initTickEvents();
    }

    static void initTickEvents() {
        if (TICK_EVENTS_REGISTERED[0]) {
            return;
        }
        TICK_EVENTS_REGISTERED[0] = true;
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

    static void shootOffhand() {
        send(GunmetalPacketRegistry.GunInput.SHOOT_OFFHAND);
    }

    static void send(GunmetalPacketRegistry.GunInput input) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        NetworkManager.sendToServer(GunmetalPacketRegistry.GUN_INPUT, GunmetalPacketRegistry.write(input, buf));
    }
}
