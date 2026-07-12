package com.xirc.militech.mixin.client;

import com.xirc.militech.client.input.MilitechKeyMappings;
import com.xirc.militech.common.item.AbstractMeleeItem;
import com.xirc.militech.registry.MilitechPacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MeleeInputMixin {
    @Shadow
    public LocalPlayer player;

    @Shadow
    public Options options;

    @Unique
    private boolean militech$primaryHeld;

    @Unique
    private boolean militech$secondaryHeld;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void militech$resetMeleeState(CallbackInfo ci) {
        if (!militech$hasMainHandMelee()) {
            militech$primaryHeld = false;
            militech$secondaryHeld = false;
            return;
        }
        if (!options.keyAttack.isDown()) {
            militech$primaryHeld = false;
        }
        if (!options.keyUse.isDown()) {
            militech$secondaryHeld = false;
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void militech$meleePrimary(CallbackInfoReturnable<Boolean> cir) {
        if (!militech$hasMainHandMelee()) {
            return;
        }
        if (!militech$primaryHeld) {
            militech$primaryHeld = true;
            MilitechKeyMappings.sendMelee(MilitechPacketRegistry.MeleeInput.PRIMARY);
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void militech$meleeSecondary(CallbackInfo ci) {
        if (!militech$hasMainHandMelee()) {
            return;
        }
        if (!militech$secondaryHeld) {
            militech$secondaryHeld = true;
            MilitechKeyMappings.sendMelee(MilitechPacketRegistry.MeleeInput.SECONDARY);
        }
        ci.cancel();
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void militech$meleeTertiary(CallbackInfo ci) {
        if (!militech$hasMainHandMelee()) {
            return;
        }
        MilitechKeyMappings.sendMelee(MilitechPacketRegistry.MeleeInput.TERTIARY);
        ci.cancel();
    }

    @Unique
    private boolean militech$hasMainHandMelee() {
        return player != null && player.getMainHandItem().getItem() instanceof AbstractMeleeItem;
    }
}
