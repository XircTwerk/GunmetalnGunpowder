package com.xirc.gunmetal.client.renderer.entity;

import com.xirc.gunmetal.common.entity.projectile.BulletProjectile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends ArrowRenderer<BulletProjectile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/projectiles/arrow.png");

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletProjectile entity) {
        return TEXTURE;
    }
}
