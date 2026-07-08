package com.xirc.militech.mixin.client;

import com.xirc.militech.client.aim.GunAimHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GunAimCrosshairMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void militech$hideCrosshairWhileAiming(GuiGraphics graphics, CallbackInfo ci) {
        if (GunAimHandler.HIDE_CROSSHAIR && GunAimHandler.isAiming()) {
            ci.cancel();
        }
    }
}
