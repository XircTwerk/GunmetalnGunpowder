package com.xirc.gunmetal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xirc.gunmetal.common.entity.projectile.BulletProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends EntityRenderer<BulletProjectile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/misc/unknown_pack.png");

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BulletProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(BulletProjectile entity) {
        return TEXTURE;
    }
}
