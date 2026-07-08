package com.xirc.militech.client.aim;

import com.xirc.militech.client.input.MilitechKeyMappings;
import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.registry.MilitechPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Client-side aim-down-sights state. Holding right-click with a main-hand gun aims;
 * the server is notified via AIM_START/AIM_STOP so it can tighten spread and slow movement.
 */
@Environment(EnvType.CLIENT)
public final class GunAimHandler {
    /** Zoom factor at full aim. */
    public static final float ZOOM = 1.5f;
    /** Hide the vanilla crosshair while aiming. */
    public static final boolean HIDE_CROSSHAIR = true;
    /** Aim-in/out speed; 0.2 = fully aimed in 5 ticks (~250 ms). */
    private static final float PROGRESS_PER_TICK = 0.2f;

    private static boolean aiming;
    private static float progress;
    private static float lastProgress;

    private GunAimHandler() {}

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            aiming = false;
            progress = 0f;
            lastProgress = 0f;
            return;
        }
        // No aiming while dual wielding: right-click is the offhand gun's trigger there.
        boolean shouldAim = minecraft.screen == null
                && !player.isSpectator()
                && player.getMainHandItem().getItem() instanceof AbstractGunItem
                && !(player.getOffhandItem().getItem() instanceof AbstractGunItem)
                && minecraft.options.keyUse.isDown();
        if (shouldAim != aiming) {
            aiming = shouldAim;
            MilitechKeyMappings.send(aiming
                    ? MilitechPacketRegistry.GunInput.AIM_START
                    : MilitechPacketRegistry.GunInput.AIM_STOP);
        }
        lastProgress = progress;
        progress = Mth.clamp(progress + (aiming ? PROGRESS_PER_TICK : -PROGRESS_PER_TICK), 0f, 1f);
    }

    public static boolean isAiming() {
        return aiming;
    }

    /** Smoothed 0→1 aim amount for visuals (FOV, gun pose). */
    public static float progress(float partialTick) {
        return Mth.lerp(partialTick, lastProgress, progress);
    }
}
