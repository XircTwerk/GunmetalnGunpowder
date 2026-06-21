package com.xirc.gunmetal.client.renderer.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.client.animation.BerettaAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import mod.azure.azurelib.render.item.AzItemRendererPipeline;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;

public class BerettaItemRenderer extends AzItemRenderer {
    private static final Set<String> EFFECT_BONES = Set.of("fire", "pressure");
    private static final ResourceLocation EFFECTS_TEXTURE = Gunmetal.id("textures/effects.png");
    private static final RenderType EFFECTS_RENDER_TYPE = RenderType.entityTranslucent(EFFECTS_TEXTURE);
    private static final DisplayPose DEFAULT_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);

    // Tweak these values when adjusting how the Beretta sits in each view.
    private static final DisplayPose GUI_POSE = new DisplayPose(0.25, -0.2, 0.05, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose FIRST_PERSON_LEFT_POSE = new DisplayPose(0.0, -0.3, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);;
    private static final DisplayPose FIRST_PERSON_RIGHT_POSE = new DisplayPose(0.0, -0.3, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);;
    private static final DisplayPose THIRD_PERSON_LEFT_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);;
    private static final DisplayPose THIRD_PERSON_RIGHT_POSE =new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);;
    private static final DisplayPose GROUND_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);;
    private static final DisplayPose FIXED_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);;

    public BerettaItemRenderer(ResourceLocation geoModel, ResourceLocation texture) {
        super(AzItemRendererConfig.builder(geoModel, texture)
                .setAnimatorProvider(BerettaAnimator::new)
                .setBoneTextureOverrideProvider(BerettaItemRenderer::effectTexture)
                .setBoneRenderTypeOverrideProvider(BerettaItemRenderer::effectRenderType)
                .build());
    }

    @Override
    protected AzItemRendererPipeline createPipeline(AzItemRendererConfig config) {
        return new AzItemRendererPipeline(config, this) {
            @Override
            public void preRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
                var itemContext = (AzItemRendererPipelineContext) context;
                poseFor(itemContext.getTransformType()).apply(context);

                super.preRender(context, isReRender);
                if (itemContext.getTransformType() == ItemDisplayContext.GUI) {
                    Lighting.setupFor3DItems();
                }
                hideEffectBones(context);
            }
        };
    }

    private void hideEffectBones(AzRendererPipelineContext<UUID, ItemStack> context) {
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

    private static DisplayPose poseFor(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> GUI_POSE;
            case FIRST_PERSON_LEFT_HAND -> FIRST_PERSON_LEFT_POSE;
            case FIRST_PERSON_RIGHT_HAND -> FIRST_PERSON_RIGHT_POSE;
            case THIRD_PERSON_LEFT_HAND -> THIRD_PERSON_LEFT_POSE;
            case THIRD_PERSON_RIGHT_HAND -> THIRD_PERSON_RIGHT_POSE;
            case GROUND -> GROUND_POSE;
            case FIXED -> FIXED_POSE;
            default -> DEFAULT_POSE;
        };
    }

    private record DisplayPose(
            double x,
            double y,
            double z,
            float xRot,
            float yRot,
            float zRot,
            float scale
    ) {
        private void apply(AzRendererPipelineContext<UUID, ItemStack> context) {
            context.poseStack().translate(x, y, z);
            context.poseStack().mulPose(Axis.XP.rotationDegrees(xRot));
            context.poseStack().mulPose(Axis.YP.rotationDegrees(yRot));
            context.poseStack().mulPose(Axis.ZP.rotationDegrees(zRot));
            context.poseStack().scale(scale, scale, scale);
        }
    }

    public static BerettaItemRenderer create() {
        return new BerettaItemRenderer(
                Gunmetal.id("geo/beretta.geo.json"),
                Gunmetal.id("textures/default.png")
        );
    }
}
