package com.xirc.gunmetal.client.renderer.item;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.client.animation.BerettaAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;

public class BerettaItemRenderer extends AbstractGunItemRenderer {
    private static final Set<String> EFFECT_BONES = Set.of("fire", "pressure");
    private static final ResourceLocation EFFECTS_TEXTURE = Gunmetal.id("textures/beretta/effects.png");
    private static final RenderType EFFECTS_RENDER_TYPE = RenderType.entityTranslucentEmissive(EFFECTS_TEXTURE);

    public BerettaItemRenderer(ResourceLocation geoModel, ResourceLocation texture) {
        super(AzItemRendererConfig.builder(geoModel, texture)
                .setAnimatorProvider(BerettaAnimator::new)
                .setBoneTextureOverrideProvider(BerettaItemRenderer::effectTexture)
                .setBoneRenderTypeOverrideProvider(BerettaItemRenderer::effectRenderType)
                .build());
    }

    //@Override
    //protected float aimPitchDegrees() {
    //    return -2f;
    //}
//
    //@Override
    //protected float aimRaise() {
    //    return 0.165f;
    //}

    @Override
    protected void afterGunPreRender(AzRendererPipelineContext<UUID, ItemStack> context) {
        var model = context.bakedModel();
        if (model == null) {
            return;
        }

        boolean firing = false;
        if (getAnimator() instanceof BerettaAnimator gun) {
            firing = gun.isFireEffectVisible();
        }
        final boolean visible = firing;

        for (String boneName : EFFECT_BONES) {
            model.getBone(boneName).ifPresent(bone -> bone.setHidden(!visible));
        }
        model.getBone("effects").ifPresent(bone -> {
            float scale = visible ? 1.0f : 0.0f;
            bone.setHidden(!visible);
            bone.setScaleX(scale);
            bone.setScaleY(scale);
            bone.setScaleZ(scale);
        });
    }

    private static ResourceLocation effectTexture(AzBone bone) {
        return EFFECT_BONES.contains(bone.getName()) ? EFFECTS_TEXTURE : null;
    }

    private static RenderType effectRenderType(AzBone bone) {
        return EFFECT_BONES.contains(bone.getName()) ? EFFECTS_RENDER_TYPE : null;
    }

    public static BerettaItemRenderer create() {
        return new BerettaItemRenderer(
                Gunmetal.id("geo/beretta.geo.json"),
                Gunmetal.id("textures/beretta/default.png")
        );
    }
}
