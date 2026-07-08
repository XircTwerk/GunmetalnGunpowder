package com.xirc.militech.mixin.client;

import com.xirc.militech.client.input.MilitechKeyMappings;
import com.xirc.militech.common.item.AbstractGunItem;
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
    private boolean militech$allowGunInteraction;

    @Unique
    private boolean militech$shotConsumed;

    @Unique
    private boolean militech$secondaryShotConsumed;

    @Unique
    private boolean militech$semiAutoAttackHeld;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void militech$handleGunKeys(CallbackInfo ci) {
        if (!militech$hasGunEquipped()) {
            militech$shotConsumed = false;
            militech$secondaryShotConsumed = false;
            militech$semiAutoAttackHeld = false;
            return;
        }
        if (!options.keyAttack.isDown()) {
            militech$shotConsumed = false;
            militech$semiAutoAttackHeld = false;
        }
        if (!options.keyUse.isDown()) {
            militech$secondaryShotConsumed = false;
        }
        while (MilitechKeyMappings.INTERACT.consumeClick()) {
            militech$allowGunInteraction = true;
            startUseItem();
            militech$allowGunInteraction = false;
        }
        AbstractGunItem mainHandGun = militech$mainHandGun();
        if (mainHandGun != null && mainHandGun.isAutomatic() && options.keyAttack.isDown()) {
            if (militech$canRepeatFire(mainHandGun, player.getMainHandItem())) {
                militech$shotConsumed = true;
                MilitechKeyMappings.shoot();
            } else if (!militech$shotConsumed) {
                militech$shotConsumed = true;
                MilitechKeyMappings.shoot();
            }
        }
        // With a gun in the offhand, right-click fires it (aiming is disabled then);
        // right-click only aims when the main hand alone holds a gun.
        AbstractGunItem offhandGun = militech$offhandGun();
        if (offhandGun != null) {
            if (offhandGun.isAutomatic() && options.keyUse.isDown()) {
                while (options.keyUse.consumeClick()) {
                }
                if (militech$canRepeatFire(offhandGun, player.getOffhandItem())) {
                    militech$secondaryShotConsumed = true;
                    MilitechKeyMappings.shootOffhand();
                } else if (!militech$secondaryShotConsumed) {
                    militech$secondaryShotConsumed = true;
                    MilitechKeyMappings.shootOffhand();
                }
            } else {
                while (options.keyUse.consumeClick()) {
                    if (!militech$secondaryShotConsumed) {
                        militech$secondaryShotConsumed = true;
                        MilitechKeyMappings.shootOffhand();
                    }
                }
            }
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void militech$shootGun(CallbackInfoReturnable<Boolean> cir) {
        AbstractGunItem mainHandGun = militech$mainHandGun();
        if (mainHandGun == null) {
            return;
        }
        if (!mainHandGun.isAutomatic()) {
            if (militech$semiAutoAttackHeld) {
                cir.setReturnValue(false);
                return;
            }
            militech$semiAutoAttackHeld = true;
        }
        if (!militech$shotConsumed) {
            militech$shotConsumed = true;
            MilitechKeyMappings.shoot();
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void militech$blockNormalUseWithGun(CallbackInfo ci) {
        if (militech$hasGunEquipped() && !militech$allowGunInteraction) {
            ci.cancel();
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void militech$blockPickWithGun(CallbackInfo ci) {
        if (militech$hasGunEquipped()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean militech$hasGunEquipped() {
        return player != null
                && (player.getMainHandItem().getItem() instanceof AbstractGunItem
                || player.getOffhandItem().getItem() instanceof AbstractGunItem);
    }

    @Unique
    private boolean militech$hasMainHandGun() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractGunItem;
    }

    @Unique
    private AbstractGunItem militech$mainHandGun() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractGunItem gun ? gun : null;
    }

    @Unique
    private AbstractGunItem militech$offhandGun() {
        return player != null && player.getOffhandItem().getItem() instanceof AbstractGunItem gun ? gun : null;
    }

    @Unique
    private boolean militech$canRepeatFire(AbstractGunItem gun, ItemStack stack) {
        return player != null && (player.isCreative() || gun.getShots(stack) > 0) && !AbstractGunItem.isReloading(stack);
    }
}
