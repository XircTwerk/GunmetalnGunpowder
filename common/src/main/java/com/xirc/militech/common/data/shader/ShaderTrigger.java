package com.xirc.militech.common.data.shader;

import com.xirc.militech.Militech;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Server-to-client trigger for a registered MilitechPostProcessor effect. */
public final class ShaderTrigger {
    public static final ResourceLocation TRIGGER_SHADER = Militech.id("trigger_shader");

    private ShaderTrigger() {
    }

    public static void sendTo(ServerPlayer player, Effect effect, float magnitude) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeEnum(effect);
        buf.writeFloat(magnitude);
        NetworkManager.sendToPlayer(player, TRIGGER_SHADER, buf);
    }

    public enum Effect {
        IMPACT_SHAKE
    }
}
