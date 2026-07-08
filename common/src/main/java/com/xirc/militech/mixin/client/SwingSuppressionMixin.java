package com.xirc.militech.mixin.client;

import com.xirc.militech.common.item.AbstractGunItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class SwingSuppressionMixin {
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void militech$suppressGunSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof LocalPlayer player)) {
            return;
        }
        if (player.getItemInHand(hand).getItem() instanceof AbstractGunItem) {
            ci.cancel();
        }
    }
}
