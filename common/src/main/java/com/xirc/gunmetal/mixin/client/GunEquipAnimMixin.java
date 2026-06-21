package com.xirc.gunmetal.mixin.client;

import com.xirc.gunmetal.common.item.AbstractGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class GunEquipAnimMixin {
    private static final float GUN_EQUIP_RAISE_SPEED = 0.4f;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private ItemStack mainHandItem;

    @Inject(method = "tick", at = @At("TAIL"))
    private void gunmetal$keepGunAtRest(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        ItemStack currentMainHand = minecraft.player.getMainHandItem();
        if (currentMainHand.getItem() instanceof AbstractGunItem) {
            boolean changedItem = !ItemStack.isSameItem(mainHandItem, currentMainHand);
            if (!ItemStack.matches(mainHandItem, currentMainHand)) {
                mainHandItem = currentMainHand.copy();
            }
            if (changedItem) {
                oMainHandHeight = 0.0f;
                mainHandHeight = 0.0f;
            } else {
                mainHandHeight += Mth.clamp(1.0f - mainHandHeight, -GUN_EQUIP_RAISE_SPEED, GUN_EQUIP_RAISE_SPEED);
            }
        }
    }
}
