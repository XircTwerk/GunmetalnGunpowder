package com.xirc.milicraft.mixin.client;

import com.xirc.milicraft.common.item.AbstractGunItem;
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
    private void milicraft$suppressGunSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof LocalPlayer player)) {
            return;
        }
        if (player.getItemInHand(hand).getItem() instanceof AbstractGunItem) {
            ci.cancel();
        }
    }
}
