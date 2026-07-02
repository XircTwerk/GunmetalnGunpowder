package com.xirc.gunmetal.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xirc.gunmetal.client.aim.GunAimHandler;
import com.xirc.gunmetal.client.aim.SightTracker;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class GunAimHandTransformMixin {
    @Inject(method = "applyItemArmTransform", at = @At("TAIL"))
    private void gunmetal$centerGunOnAim(PoseStack poseStack, HumanoidArm arm, float equippedProgress, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || arm != mc.player.getMainArm()) return;
        float aim = GunAimHandler.progress(mc.getFrameTime());
        if (aim <= 0f) {
            SightTracker.recordApplied(Vec3.ZERO);
            return;
        }
        Vec3 rest = SightTracker.rest();
        Vec3 applied;
        if (rest != null) {
            // Slide the gun so the measured back_sight position lands on the camera's
            // center line.
            applied = new Vec3(-rest.x * aim, -rest.y * aim, 0.0);
        } else {
            // No back_sight bone: cancel vanilla's ±0.56 hand offset plus the item
            // model's own first-person X display translation instead.
            int side = arm == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack stack = mc.player.getMainHandItem();
            ItemDisplayContext displayCtx = arm == HumanoidArm.RIGHT
                    ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                    : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            ItemTransform transform = mc.getItemRenderer()
                    .getModel(stack, mc.level, mc.player, 0)
                    .getTransforms().getTransform(displayCtx);
            float displayX = side * transform.translation.x();
            applied = new Vec3((-side * 0.56f - displayX) * aim, 0.0, 0.0);
        }
        poseStack.translate(applied.x, applied.y, applied.z);
        SightTracker.recordApplied(applied);
    }
}
