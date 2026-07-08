package com.xirc.militech.mixin.client;

import com.xirc.militech.common.item.AbstractGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class GunEquipAnimMixin {
    @Unique
    private static final float GUN_EQUIP_RAISE_SPEED = 0.4f;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private ItemStack offHandItem;

    @Inject(method = "tick", at = @At("TAIL"))
    private void militech$keepGunAtRest(CallbackInfo ci) {
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

        ItemStack currentOffHand = minecraft.player.getOffhandItem();
        if (currentOffHand.getItem() instanceof AbstractGunItem) {
            boolean changedItem = !ItemStack.isSameItem(offHandItem, currentOffHand);
            if (!ItemStack.matches(offHandItem, currentOffHand)) {
                offHandItem = currentOffHand.copy();
            }
            if (changedItem) {
                oOffHandHeight = 0.0f;
                offHandHeight = 0.0f;
            } else {
                offHandHeight += Mth.clamp(1.0f - offHandHeight, -GUN_EQUIP_RAISE_SPEED, GUN_EQUIP_RAISE_SPEED);
            }
        }
    }
}
