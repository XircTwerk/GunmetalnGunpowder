package com.xirc.militech.client.input;

import com.xirc.militech.client.aim.GunAimHandler;
import com.xirc.militech.client.tracer.MuzzleTracker;
import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.registry.MilitechPacketRegistry;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public interface MilitechKeyMappings {
    boolean[] TICK_EVENTS_REGISTERED = {false};

    KeyMapping RELOAD = new KeyMapping(
            "key.militech.reload",
            GLFW.GLFW_KEY_R,
            "key.categories.militech"
    );
    KeyMapping INTERACT = new KeyMapping(
            "key.militech.interact",
            GLFW.GLFW_KEY_I,
            "key.categories.militech"
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
        ClientTickEvent.CLIENT_POST.register(MilitechKeyMappings::tick);
    }

    static void tick(Minecraft minecraft) {
        GunAimHandler.tick(minecraft);
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (RELOAD.consumeClick()) {
            if (hasGunEquipped(minecraft)) {
                send(MilitechPacketRegistry.GunInput.RELOAD);
            }
        }
    }

    static boolean hasGunEquipped(Minecraft minecraft) {
        return minecraft.player != null
                && (minecraft.player.getMainHandItem().getItem() instanceof AbstractGunItem
                || minecraft.player.getOffhandItem().getItem() instanceof AbstractGunItem);
    }

    static void shoot() {
        send(MilitechPacketRegistry.GunInput.SHOOT);
    }

    static void shootOffhand() {
        send(MilitechPacketRegistry.GunInput.SHOOT_OFFHAND);
    }

    static void send(MilitechPacketRegistry.GunInput input) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        Minecraft minecraft = Minecraft.getInstance();
        // The effects bone is only captured while the first-person hand renders;
        // in any other camera mode the stored position would be stale.
        Vec3 muzzle = null;
        if (minecraft.player != null && minecraft.options.getCameraType().isFirstPerson()) {
            if (input == MilitechPacketRegistry.GunInput.SHOOT) {
                muzzle = MuzzleTracker.get(minecraft.player.getUUID(), InteractionHand.MAIN_HAND);
            } else if (input == MilitechPacketRegistry.GunInput.SHOOT_OFFHAND) {
                muzzle = MuzzleTracker.get(minecraft.player.getUUID(), InteractionHand.OFF_HAND);
            }
        }
        NetworkManager.sendToServer(MilitechPacketRegistry.GUN_INPUT, MilitechPacketRegistry.write(input, muzzle, buf));
    }
}
