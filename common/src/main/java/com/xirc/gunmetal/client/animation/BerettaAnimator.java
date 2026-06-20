package com.xirc.gunmetal.client.animation;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BerettaAnimator extends AzItemAnimator {
    private static final ResourceLocation ANIMATION = Gunmetal.id("beretta.animation.json");
    private static final String CONTROLLER = "gun";

    private AzAnimationController<ItemStack> controller;
    private long lastSequence = Long.MIN_VALUE;
    private boolean idleDispatched;

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

        long sequence = stack.getOrCreateTag().getLong(AbstractGunItem.ANIMATION_SEQUENCE_ID);
        if (sequence != lastSequence) {
            lastSequence = sequence;
            String animation = stack.getOrCreateTag().getString(AbstractGunItem.ANIMATION_ID);
            if (!animation.isEmpty()) {
                idleDispatched = false;
                dispatch(animation, AzPlayBehaviors.PLAY_ONCE);
                return;
            }
        }

        if (!idleDispatched
                && !controller.stateMachine().isPlaying()
                && !controller.stateMachine().isTransitioning()) {
            idleDispatched = true;
            dispatch("idle", AzPlayBehaviors.LOOP);
        }
    }

    public String currentAnimationName() {
        if (controller == null || controller.currentAnimation() == null) {
            return null;
        }
        return controller.currentAnimation().animation().name();
    }

    @Override
    public ResourceLocation getAnimationLocation(ItemStack stack) {
        return ANIMATION;
    }

    private void dispatch(String animation, AzPlayBehavior behavior) {
        AzCommand.create(CONTROLLER, animation, behavior)
                .actions()
                .forEach(action -> action.handle(AzDispatchSide.CLIENT, this));
    }
}
