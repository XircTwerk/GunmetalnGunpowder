package com.xirc.milicraft.mixin.client;

import com.xirc.milicraft.client.hud.GunHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class GunHudToastLayerMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void milicraft$renderGunHudAboveToasts(GuiGraphics graphics, CallbackInfo ci) {
        GunHud.renderAboveToasts(graphics);
    }
}
