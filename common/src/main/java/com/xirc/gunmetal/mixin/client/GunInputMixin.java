package com.xirc.gunmetal.mixin.client;

import com.xirc.gunmetal.client.input.GunmetalKeyMappings;
import com.xirc.gunmetal.common.item.AbstractGunItem;
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
    private boolean gunmetal$allowGunInteraction;

    @Unique
    private boolean gunmetal$shotConsumed;

    @Unique
    private boolean gunmetal$secondaryShotConsumed;

    @Unique
    private boolean gunmetal$semiAutoAttackHeld;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void gunmetal$handleGunKeys(CallbackInfo ci) {
        if (!gunmetal$hasGunEquipped()) {
            gunmetal$shotConsumed = false;
            gunmetal$secondaryShotConsumed = false;
            gunmetal$semiAutoAttackHeld = false;
            return;
        }
        if (!options.keyAttack.isDown()) {
            gunmetal$shotConsumed = false;
            gunmetal$semiAutoAttackHeld = false;
        }
        if (!options.keyUse.isDown()) {
            gunmetal$secondaryShotConsumed = false;
        }
        while (GunmetalKeyMappings.INTERACT.consumeClick()) {
            gunmetal$allowGunInteraction = true;
            startUseItem();
            gunmetal$allowGunInteraction = false;
        }
        AbstractGunItem mainHandGun = gunmetal$mainHandGun();
        if (mainHandGun != null && mainHandGun.isAutomatic() && options.keyAttack.isDown()) {
            if (gunmetal$canRepeatFire(mainHandGun, player.getMainHandItem())) {
                gunmetal$shotConsumed = true;
                GunmetalKeyMappings.shoot();
            } else if (!gunmetal$shotConsumed) {
                gunmetal$shotConsumed = true;
                GunmetalKeyMappings.shoot();
            }
        }
        // With a gun in the offhand, right-click fires it (aiming is disabled then);
        // right-click only aims when the main hand alone holds a gun.
        AbstractGunItem offhandGun = gunmetal$offhandGun();
        if (offhandGun != null) {
            if (offhandGun.isAutomatic() && options.keyUse.isDown()) {
                while (options.keyUse.consumeClick()) {
                }
                if (gunmetal$canRepeatFire(offhandGun, player.getOffhandItem())) {
                    gunmetal$secondaryShotConsumed = true;
                    GunmetalKeyMappings.shootOffhand();
                } else if (!gunmetal$secondaryShotConsumed) {
                    gunmetal$secondaryShotConsumed = true;
                    GunmetalKeyMappings.shootOffhand();
                }
            } else {
                while (options.keyUse.consumeClick()) {
                    if (!gunmetal$secondaryShotConsumed) {
                        gunmetal$secondaryShotConsumed = true;
                        GunmetalKeyMappings.shootOffhand();
                    }
                }
            }
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void gunmetal$shootGun(CallbackInfoReturnable<Boolean> cir) {
        AbstractGunItem mainHandGun = gunmetal$mainHandGun();
        if (mainHandGun == null) {
            return;
        }
        if (!mainHandGun.isAutomatic()) {
            if (gunmetal$semiAutoAttackHeld) {
                cir.setReturnValue(false);
                return;
            }
            gunmetal$semiAutoAttackHeld = true;
        }
        if (!gunmetal$shotConsumed) {
            gunmetal$shotConsumed = true;
            GunmetalKeyMappings.shoot();
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void gunmetal$blockNormalUseWithGun(CallbackInfo ci) {
        if (gunmetal$hasGunEquipped() && !gunmetal$allowGunInteraction) {
            ci.cancel();
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void gunmetal$blockPickWithGun(CallbackInfo ci) {
        if (gunmetal$hasGunEquipped()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean gunmetal$hasGunEquipped() {
        return player != null
                && (player.getMainHandItem().getItem() instanceof AbstractGunItem
                || player.getOffhandItem().getItem() instanceof AbstractGunItem);
    }

    @Unique
    private boolean gunmetal$hasMainHandGun() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractGunItem;
    }

    @Unique
    private AbstractGunItem gunmetal$mainHandGun() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractGunItem gun ? gun : null;
    }

    @Unique
    private AbstractGunItem gunmetal$offhandGun() {
        return player != null && player.getOffhandItem().getItem() instanceof AbstractGunItem gun ? gun : null;
    }

    @Unique
    private boolean gunmetal$canRepeatFire(AbstractGunItem gun, ItemStack stack) {
        return player != null && (player.isCreative() || gun.getShots(stack) > 0) && !AbstractGunItem.isReloading(stack);
    }
}
