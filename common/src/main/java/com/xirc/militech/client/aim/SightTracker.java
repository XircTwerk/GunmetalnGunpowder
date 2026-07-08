package com.xirc.militech.client.aim;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

/**
 * View-space position of the main-hand gun's back_sight bone, measured while it renders.
 * The aim transform slides the gun by the negative of this so the sight lands on the
 * camera's center line, replacing hand-tuned per-gun offsets.
 */
@Environment(EnvType.CLIENT)
public final class SightTracker {
    private static final long FRESH_NANOS = 250_000_000L;

    private static Vec3 applied = Vec3.ZERO;
    private static Vec3 rest;
    private static long capturedAt;

    private SightTracker() {}

    /** Aim translate applied this frame, recorded so the measurement can exclude it. */
    public static void recordApplied(Vec3 translate) {
        applied = translate;
    }

    /** Called with the bone's rendered view-space position; stores it minus the applied translate. */
    public static void recordMeasured(Vec3 viewPos) {
        rest = viewPos.subtract(applied);
        capturedAt = System.nanoTime();
    }

    /** Sight rest position in view space, or null if no back_sight bone rendered recently. */
    public static Vec3 rest() {
        return rest != null && System.nanoTime() - capturedAt < FRESH_NANOS ? rest : null;
    }
}
