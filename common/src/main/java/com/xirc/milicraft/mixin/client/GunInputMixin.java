package com.xirc.milicraft.mixin.client;

import com.xirc.milicraft.client.input.MilicraftKeyMappings;
import com.xirc.milicraft.common.item.AbstractGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class GunInputMixin {
    @Shadow
    public LocalPlayer player;

    @Shadow
    public Options options;

    @Shadow
    private void startUseItem() {
    }

    @Unique
    private boolean milicraft$allowGunInteraction;

    @Unique
    private boolean milicraft$shotConsumed;

    @Unique
    private boolean milicraft$secondaryShotConsumed;

    @Unique
    private boolean milicraft$semiAutoAttackHeld;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void milicraft$handleGunKeys(CallbackInfo ci) {
        if (!milicraft$hasGunEquipped()) {
            milicraft$shotConsumed = false;
            milicraft$secondaryShotConsumed = false;
            milicraft$semiAutoAttackHeld = false;
            return;
        }
        if (!options.keyAttack.isDown()) {
            milicraft$shotConsumed = false;
            milicraft$semiAutoAttackHeld = false;
        }
        if (!options.keyUse.isDown()) {
            milicraft$secondaryShotConsumed = false;
        }
        while (MilicraftKeyMappings.INTERACT.consumeClick()) {
            milicraft$allowGunInteraction = true;
            startUseItem();
            milicraft$allowGunInteraction = false;
        }
        AbstractGunItem mainHandGun = milicraft$mainHandGun();
        if (mainHandGun != null && mainHandGun.isAutomatic() && options.keyAttack.isDown()) {
            if (milicraft$canRepeatFire(mainHandGun, player.getMainHandItem())) {
                milicraft$shotConsumed = true;
                MilicraftKeyMappings.shoot();
            } else if (!milicraft$shotConsumed) {
                milicraft$shotConsumed = true;
                MilicraftKeyMappings.shoot();
            }
        }
        // With a gun in the offhand, right-click fires it (aiming is disabled then);
        // right-click only aims when the main hand alone holds a gun.
        AbstractGunItem offhandGun = milicraft$offhandGun();
        if (offhandGun != null) {
            if (offhandGun.isAutomatic() && options.keyUse.isDown()) {
                while (options.keyUse.consumeClick()) {
                }
                if (milicraft$canRepeatFire(offhandGun, player.getOffhandItem())) {
                    milicraft$secondaryShotConsumed = true;
                    MilicraftKeyMappings.shootOffhand();
                } else if (!milicraft$secondaryShotConsumed) {
                    milicraft$secondaryShotConsumed = true;
                    MilicraftKeyMappings.shootOffhand();
                }
            } else {
                while (options.keyUse.consumeClick()) {
                    if (!milicraft$secondaryShotConsumed) {
                        milicraft$secondaryShotConsumed = true;
                        MilicraftKeyMappings.shootOffhand();
                    }
                }
            }
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void milicraft$shootGun(CallbackInfoReturnable<Boolean> cir) {
        AbstractGunItem mainHandGun = milicraft$mainHandGun();
        if (mainHandGun == null) {
            return;
        }
        if (!mainHandGun.isAutomatic()) {
            if (milicraft$semiAutoAttackHeld) {
                cir.setReturnValue(false);
                return;
            }
            milicraft$semiAutoAttackHeld = true;
        }
        if (!milicraft$shotConsumed) {
            milicraft$shotConsumed = true;
            MilicraftKeyMappings.shoot();
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void milicraft$blockNormalUseWithGun(CallbackInfo ci) {
        if (milicraft$hasGunEquipped() && !milicraft$allowGunInteraction) {
            ci.cancel();
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void milicraft$blockPickWithGun(CallbackInfo ci) {
        if (milicraft$hasGunEquipped()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean milicraft$hasGunEquipped() {
        return player != null
                && (player.getMainHandItem().getItem() instanceof AbstractGunItem
                || player.getOffhandItem().getItem() instanceof AbstractGunItem);
    }

    @Unique
    private boolean milicraft$hasMainHandGun() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractGunItem;
    }

    @Unique
    private AbstractGunItem milicraft$mainHandGun() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractGunItem gun ? gun : null;
    }

    @Unique
    private AbstractGunItem milicraft$offhandGun() {
        return player != null && player.getOffhandItem().getItem() instanceof AbstractGunItem gun ? gun : null;
    }

    @Unique
    private boolean milicraft$canRepeatFire(AbstractGunItem gun, ItemStack stack) {
        return player != null && (player.isCreative() || gun.getShots(stack) > 0) && !AbstractGunItem.isReloading(stack);
    }
}
