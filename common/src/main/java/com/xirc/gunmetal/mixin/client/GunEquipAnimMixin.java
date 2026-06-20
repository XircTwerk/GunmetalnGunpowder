package com.xirc.gunmetal.mixin.client;

import com.xirc.gunmetal.common.item.AbstractGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class GunEquipAnimMixin {
    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Inject(method = "tick", at = @At("TAIL"))
    private void gunmetal$keepGunAtRest(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (minecraft.player.getMainHandItem().getItem() instanceof AbstractGunItem) {
            oMainHandHeight = 1.0f;
            mainHandHeight = 1.0f;
        }
    }
}
