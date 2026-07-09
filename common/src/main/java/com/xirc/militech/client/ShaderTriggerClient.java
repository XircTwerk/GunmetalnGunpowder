package com.xirc.militech.client;

import com.xirc.militech.client.shader.ImpactShakeShaderEffect;
import com.xirc.militech.common.data.shader.ShaderTrigger;
import dev.architectury.networking.NetworkManager;

/** Client-side receiver for {@link ShaderTrigger}; fires the matching post-process effect. */
public final class ShaderTriggerClient {
    private ShaderTriggerClient() {
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ShaderTrigger.TRIGGER_SHADER, (buf, context) -> {
            ShaderTrigger.Effect effect = buf.readEnum(ShaderTrigger.Effect.class);
            float magnitude = buf.readFloat();
            context.queue(() -> {
                switch (effect) {
                    case IMPACT_SHAKE -> ImpactShakeShaderEffect.getInstance().trigger(magnitude);
                }
            });
        });
    }
}
