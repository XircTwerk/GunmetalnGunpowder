package com.xirc.gunmetal.client.renderer.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.client.animation.M16Animator;
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

public class M16ItemRenderer extends AzItemRenderer {
    private static final Set<String> EFFECT_BONES = Set.of("effects", "fire");
    private static final Set<String> FOREGRIP_BONES = Set.of("handgaurd");
    private static final ResourceLocation EFFECTS_TEXTURE = Gunmetal.id("textures/m16/effects.png");
    private static final ResourceLocation FOREGRIP_TEXTURE = Gunmetal.id("textures/m16/foregrip_plastic.png");
    private static final RenderType EFFECTS_RENDER_TYPE = RenderType.entityTranslucentEmissive(EFFECTS_TEXTURE);
    private static final RenderType FOREGRIP_RENDER_TYPE = RenderType.entityCutoutNoCull(FOREGRIP_TEXTURE);
    private static final DisplayPose DEFAULT_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);

    // Tweak these values when adjusting how the M16 sits in each view.
    private static final DisplayPose GUI_POSE = new DisplayPose(0.0, -0.2, 0.0, 0.0f, 0.0f, 0.0f, 0.75f);
    private static final DisplayPose FIRST_PERSON_LEFT_POSE = new DisplayPose(0.0, -0.35, 0.0, 0.0f, 0.0f, 0.0f, 0.9f);
    private static final DisplayPose FIRST_PERSON_RIGHT_POSE = new DisplayPose(0.0, -0.35, 0.0, 0.0f, 0.0f, 0.0f, 0.9f);
    private static final DisplayPose THIRD_PERSON_LEFT_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.9f);
    private static final DisplayPose THIRD_PERSON_RIGHT_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.9f);
    private static final DisplayPose GROUND_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.8f);
    private static final DisplayPose FIXED_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.8f);

    public M16ItemRenderer(ResourceLocation geoModel, ResourceLocation texture) {
        super(AzItemRendererConfig.builder(geoModel, texture)
                .setAnimatorProvider(M16Animator::new)
                .setBoneTextureOverrideProvider(M16ItemRenderer::effectTexture)
                .setBoneRenderTypeOverrideProvider(M16ItemRenderer::effectRenderType)
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

        boolean visible = getAnimator() instanceof M16Animator gun && gun.isFireEffectVisible();
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
        if (EFFECT_BONES.contains(bone.getName())) {
            return EFFECTS_TEXTURE;
        }
        if (FOREGRIP_BONES.contains(bone.getName())) {
            return FOREGRIP_TEXTURE;
        }
        return null;
    }

    private static RenderType effectRenderType(AzBone bone) {
        if (EFFECT_BONES.contains(bone.getName())) {
            return EFFECTS_RENDER_TYPE;
        }
        if (FOREGRIP_BONES.contains(bone.getName())) {
            return FOREGRIP_RENDER_TYPE;
        }
        return null;
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

    public static M16ItemRenderer create() {
        return new M16ItemRenderer(
                Gunmetal.id("geo/m16.geo.json"),
                Gunmetal.id("textures/m16/default.png")
        );
    }
}
