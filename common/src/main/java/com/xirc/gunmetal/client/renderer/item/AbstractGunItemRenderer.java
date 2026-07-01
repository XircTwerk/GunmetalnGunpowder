package com.xirc.gunmetal.client.renderer.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import com.xirc.gunmetal.client.tracer.MuzzleTracker;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import mod.azure.azurelib.render.item.AzItemRendererPipeline;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.UUID;

public abstract class AbstractGunItemRenderer extends AzItemRenderer {
    private static final DisplayPose DEFAULT_POSE = new DisplayPose(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose GUI_POSE = new DisplayPose(-0.3f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose FIRST_PERSON_LEFT_POSE = new DisplayPose(0.0f, -0.2f, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose FIRST_PERSON_RIGHT_POSE = new DisplayPose(0.0f, -0.2f, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose THIRD_PERSON_LEFT_POSE = new DisplayPose(0.0f, -0.4f, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose THIRD_PERSON_RIGHT_POSE = new DisplayPose(0.0f, -0.4f, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose GROUND_POSE = new DisplayPose(0.0f, -0.5f, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);
    private static final DisplayPose FIXED_POSE = new DisplayPose(0.0f, -0.5f, 0.0, 0.0f, 0.0f, 0.0f, 1.0f);

    protected AbstractGunItemRenderer(AzItemRendererConfig config) {
        super(config);
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
                afterGunPreRender(context);
            }

            @Override
            public void postRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
                super.postRender(context, isReRender);
                var itemContext = (AzItemRendererPipelineContext) context;
                var displayCtx = itemContext.getTransformType();
                if (displayCtx != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        && displayCtx != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                    return;
                }
                var model = context.bakedModel();
                if (model == null) return;
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return;
                model.getBone("effects").ifPresent(bone -> {
                    // modelRenderTranslations is captured just before per-bone rendering and encodes
                    // the full model-to-view transform (arm position, display pose, AzureLib offset).
                    // Transforming the effects bone's pivot (pixels → /16 → blocks) gives its exact
                    // view-space position. This is then converted to world-space via camera vectors.
                    Vector4f pivotVS = new Vector4f(
                            bone.getPivotX() / 16f,
                            bone.getPivotY() / 16f,
                            bone.getPivotZ() / 16f,
                            1f);
                    modelRenderTranslations.transform(pivotVS);

                    Camera cam = mc.gameRenderer.getMainCamera();
                    Vec3 camPos = cam.getPosition();
                    Vec3 look = mc.player.getLookAngle();
                    org.joml.Vector3f upVF = cam.getUpVector();
                    Vec3 up = new Vec3(upVF.x, upVF.y, upVF.z);
                    Vec3 right = look.cross(up);

                    Vec3 muzzle = camPos
                            .add(right.scale(pivotVS.x))
                            .add(up.scale(pivotVS.y))
                            .subtract(look.scale(pivotVS.z));
                    MuzzleTracker.record(mc.player.getUUID(), muzzle);
                });
            }
        };
    }

    protected void afterGunPreRender(AzRendererPipelineContext<UUID, ItemStack> context) {
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
}
