package com.xirc.milicraft.mixin.client;

import com.xirc.milicraft.client.aim.GunAimHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GunAimFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void milicraft$aimZoom(Camera camera, float partialTick, boolean useFOVSetting,
                                  CallbackInfoReturnable<Double> cir) {
        // Only zoom the world FOV, not the fixed hand FOV.
        if (!useFOVSetting) return;
        float progress = GunAimHandler.progress(partialTick);
        if (progress <= 0f) return;
        cir.setReturnValue(cir.getReturnValue() / Mth.lerp(progress, 1.0f, GunAimHandler.ZOOM));
    }
}
