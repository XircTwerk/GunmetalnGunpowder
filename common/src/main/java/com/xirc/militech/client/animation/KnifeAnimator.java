package com.xirc.militech.client.animation;

import com.xirc.militech.Militech;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class KnifeAnimator extends AzItemAnimator {
    private static final ResourceLocation ANIMATION = Militech.id("animations/knife.animation.json");

    @Override
    public void registerControllers(AzAnimationControllerContainer<ItemStack> container) {
        container.add(AzAnimationController.builder(this, "knife").build());
    }

    @Override
    public ResourceLocation getAnimationLocation(ItemStack stack) {
        return ANIMATION;
    }
}
