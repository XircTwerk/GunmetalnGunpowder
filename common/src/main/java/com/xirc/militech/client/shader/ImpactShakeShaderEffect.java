package com.xirc.militech.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xirc.militech.Militech;
import net.minecraft.resources.ResourceLocation;

/**
 * Impact Shake + Flash post-process effect.
 *
 * Sequence (all values in seconds):
 *   0.000 – 0.050  Hard white-out flash (instant)
 *   0.050 – 0.175  Bloom hold: center bright, edges clear first
 *   0.175 – 0.350  Fast fade to clear
 *   0.070+         Screen shake kicks in (delayed past flash), decays fast
 */
public class ImpactShakeShaderEffect extends MilitechPostProcessor {

    private static final ImpactShakeShaderEffect INSTANCE = new ImpactShakeShaderEffect();
    public static ImpactShakeShaderEffect getInstance() { return INSTANCE; }
    private ImpactShakeShaderEffect() {}

    // Timing (seconds)
    private static final float DURATION        = 0.60f;
    private static final int   FADE_IN_TICKS   = 1;   // near-instant
    private static final int   FADE_OUT_TICKS  = 6;

    // State
    private float elapsedSeconds  = 0f;
    private float intensity       = 0f;
    /** Scales the shake amplitude. Pass >1.0 for harder hits. */
    private float shakeMagnitude  = 1.0f;

    // Shake internals
    private static final float MAX_SHAKE_PX     = 24f;
    private static final float SHAKE_DECAY      = 9f;
    private static final float SHAKE_DELAY      = 0.07f;
    private static final float MAX_CHROMA_SPLIT = 0.022f;

    @Override
    public ResourceLocation getShaderEffectId() {
        return new ResourceLocation(Militech.MOD_ID, "impact_shake");
    }

    @Override
    protected void beforeProcess(PoseStack viewModelStack) {
        if (effects == null || effects.length == 0) return;

        elapsedSeconds += frameDeltaSeconds;

        float progress = Math.min(elapsedSeconds / DURATION, 1.0f);
        setUniform("Progress", progress);

        int elapsedTicks = (int) (elapsedSeconds * 20f);
        int totalTicks   = (int) (DURATION * 20f);
        if (elapsedTicks <= FADE_IN_TICKS) {
            intensity = (float) elapsedTicks / FADE_IN_TICKS;
        } else if (elapsedTicks > totalTicks - FADE_OUT_TICKS) {
            intensity = (float) (totalTicks - elapsedTicks) / FADE_OUT_TICKS;
        } else {
            intensity = 1.0f;
        }
        intensity = Math.max(0f, Math.min(1f, intensity));
        setUniform("Intensity", intensity);

        float shakeT   = Math.max(0f, elapsedSeconds - SHAKE_DELAY);
        float shakeAmp = shakeMagnitude * MAX_SHAKE_PX * (float) Math.exp(-shakeT * SHAKE_DECAY);

        int   frameBucket = (int) (elapsedSeconds * 60f);
        float rx = (float) (Math.sin(frameBucket * 127.1 + 311.7) * 43758.5453 % 1.0);
        float ry = (float) (Math.sin((frameBucket + 100) * 127.1 + 311.7) * 43758.5453 % 1.0);
        float shakeX = (rx * 2f - 1f) * shakeAmp;
        float shakeY = (ry * 2f - 1f) * shakeAmp * 0.55f;

        int screenW = MC.getWindow().getScreenWidth();
        int screenH = MC.getWindow().getScreenHeight();
        setUniform("ShakeX", screenW > 0 ? shakeX / screenW : 0f);
        setUniform("ShakeY", screenH > 0 ? shakeY / screenH : 0f);

        float normalizedAmp = Math.min(shakeAmp / MAX_SHAKE_PX, 1.0f);
        setUniform("ChromaSplit", normalizedAmp * MAX_CHROMA_SPLIT);

        if (elapsedSeconds >= DURATION) {
            setActive(false);
        }
    }

    /**
     * Fire the effect with default magnitude (1.0).
     */
    public void trigger() {
        trigger(1.0f);
    }

    /**
     * Fire the effect with a custom shake magnitude.
     * @param magnitude 1.0 = normal hit. Values above 2.0 not recommended.
     */
    public void trigger(float magnitude) {
        float remaining = isActive() ? Math.max(0f, DURATION - elapsedSeconds) : 0f;
        if (remaining > DURATION * 0.5f && magnitude < shakeMagnitude * 1.3f) {
            return;
        }
        elapsedSeconds = 0f;
        intensity = 0f;
        shakeMagnitude = Math.max(0f, magnitude);
        setActive(true);
    }

    /**
     * Trigger the effect scaled by raw damage and stun ticks.
     * base = (damage/20 + stunTicks/20) * 0.5, clamped to [0.2, 2.0]
     */
    public void triggerWithDamageStun(float damage, int stunTicks) {
        float mag = (damage / 20.0f + stunTicks / 20.0f) * 0.5f;
        mag = Math.max(0.2f, Math.min(2.0f, mag));
        trigger(mag);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (!active) {
            elapsedSeconds = 0f;
            intensity      = 0f;
        }
    }

    private void setUniform(String name, float value) {
        Uniform u = effects[0].getUniform(name);
        if (u != null) u.set(value);
    }
}
