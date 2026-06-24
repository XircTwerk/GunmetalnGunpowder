package com.xirc.gunmetal.client.animation;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class AssaultRifleAnimator extends AzItemAnimator {
    private static final ResourceLocation ANIMATION = Gunmetal.id("animations/assault_rifle.animation.json");
    private static final String CONTROLLER = "gun";

    private AzAnimationController<ItemStack> controller;
    private long lastSequence = Long.MIN_VALUE;
    private UUID lastStackId;
    private long fireEffectEndsAt;
    private boolean sequenceInitialized;

    @Override
    public void registerControllers(AzAnimationControllerContainer<ItemStack> container) {
        controller = AzAnimationController.builder(this, CONTROLLER).build();
        container.add(controller);
    }

    @Override
    public void setCustomAnimations(ItemStack stack, float partialTicks) {
        if (controller == null) {
            return;
        }

        UUID stackId = stackId(stack);
        long sequence = stack.getOrCreateTag().getLong(AbstractGunItem.ANIMATION_SEQUENCE_ID);
        if (!sequenceInitialized || stackChanged(stackId)) {
            sequenceInitialized = true;
            lastStackId = stackId;
            lastSequence = sequence;
            return;
        }

        if (sequence != lastSequence) {
            lastSequence = sequence;
            String animation = stack.getOrCreateTag().getString(AbstractGunItem.ANIMATION_ID);
            if (!animation.isEmpty()) {
                if (animation.startsWith("fire")) {
                    resetController();
                }
                dispatch(animation);
                markEffectWindow(animation);
            }
        }
    }

    public boolean isFireEffectVisible() {
        return System.nanoTime() < fireEffectEndsAt;
    }

    @Override
    public ResourceLocation getAnimationLocation(ItemStack stack) {
        return ANIMATION;
    }

    private void dispatch(String animation) {
        AzCommand.create(CONTROLLER, animation, AzPlayBehaviors.PLAY_ONCE)
                .actions()
                .forEach(action -> action.handle(AzDispatchSide.CLIENT, this));
    }

    private void resetController() {
        controller.animationQueue().clear();
        controller.controllerTimer().reset();
        controller.keyframeManager().keyframeCallbackHandler().reset();
        controller.setCurrentAnimation(null);
        controller.stateMachine().stop();
    }

    private void markEffectWindow(String animation) {
        fireEffectEndsAt = animation.startsWith("fire") ? System.nanoTime() + 125_000_000L : 0L;
    }

    private boolean stackChanged(UUID stackId) {
        return stackId != null && !stackId.equals(lastStackId);
    }

    private static UUID stackId(ItemStack stack) {
        return stack.getOrCreateTag().hasUUID(AzureLib.ITEM_UUID_TAG)
                ? stack.getOrCreateTag().getUUID(AzureLib.ITEM_UUID_TAG)
                : null;
    }
}
