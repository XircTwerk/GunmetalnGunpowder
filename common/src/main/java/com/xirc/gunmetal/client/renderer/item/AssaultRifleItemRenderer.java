package com.xirc.gunmetal.client.renderer.item;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.client.animation.AssaultRifleAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;

public class AssaultRifleItemRenderer extends AbstractGunItemRenderer {
    private static final Set<String> EFFECT_BONES = Set.of("effects", "fire");
    private static final ResourceLocation EFFECTS_TEXTURE = Gunmetal.id("textures/assault_rifle/effects.png");
    private static final RenderType EFFECTS_RENDER_TYPE = RenderType.entityTranslucentEmissive(EFFECTS_TEXTURE);

    public AssaultRifleItemRenderer(ResourceLocation geoModel, ResourceLocation texture) {
        super(AzItemRendererConfig.builder(geoModel, texture)
                .setAnimatorProvider(AssaultRifleAnimator::new)
                .setBoneTextureOverrideProvider(AssaultRifleItemRenderer::effectTexture)
                .setBoneRenderTypeOverrideProvider(AssaultRifleItemRenderer::effectRenderType)
                .build());
    }

    @Override
    protected float aimSetback() {
        // The tall receiver clips the near plane when aimed; tune in-game.
        return -0.2f;
    }

    //@Override
    //protected float aimPitchDegrees() {
    //    return 2f;
    //}
//
    //@Override
    //protected float aimRaise() {
    //    return 0.15f;
    //}

    @Override
    protected void afterGunPreRender(AzRendererPipelineContext<UUID, ItemStack> context) {
        var model = context.bakedModel();
        if (model == null) {
            return;
        }

        boolean visible = getAnimator() instanceof AssaultRifleAnimator gun && gun.isFireEffectVisible();
        for (String boneName : EFFECT_BONES) {
            model.getBone(boneName).ifPresent(bone -> {
                float scale = visible ? 1.0f : 0.0f;
                bone.setHidden(!visible);
                bone.setScaleX(scale);
                bone.setScaleY(scale);
                bone.setScaleZ(scale);
            });
        }
    }

    private static ResourceLocation effectTexture(AzBone bone) {
        return EFFECT_BONES.contains(bone.getName()) ? EFFECTS_TEXTURE : null;
    }

    private static RenderType effectRenderType(AzBone bone) {
        return EFFECT_BONES.contains(bone.getName()) ? EFFECTS_RENDER_TYPE : null;
    }

    public static AssaultRifleItemRenderer create() {
        return new AssaultRifleItemRenderer(
                Gunmetal.id("geo/assault_rifle.geo.json"),
                Gunmetal.id("textures/assault_rifle/default.png")
        );
    }
}
