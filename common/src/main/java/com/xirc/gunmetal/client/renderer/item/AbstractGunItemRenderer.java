package com.xirc.gunmetal.client.renderer.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import com.xirc.gunmetal.client.aim.GunAimHandler;
import com.xirc.gunmetal.client.aim.SightTracker;
import com.xirc.gunmetal.client.tracer.MuzzleTracker;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import mod.azure.azurelib.render.item.AzItemRendererPipeline;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
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

    // Vertical/depth slide of the first-person gun at full aim; tune in-game.
    // Horizontal centering happens in GunAimHandTransformMixin.
    private static final float AIM_CENTER_Y = 0.15f;
    private static final float AIM_CENTER_Z = 0.0f;

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
                applyAimPose(context, itemContext.getTransformType());

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
                boolean rightCtx = displayCtx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                boolean rightMainArm = mc.player.getMainArm() == HumanoidArm.RIGHT;
                InteractionHand hand = rightCtx == rightMainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (hand == InteractionHand.MAIN_HAND) {
                    model.getBone("back_sight").ifPresent(bone -> {
                        Vector4f pivotVS = new Vector4f(
                                bone.getPivotX() / 16f,
                                bone.getPivotY() / 16f,
                                bone.getPivotZ() / 16f,
                                1f);
                        modelRenderTranslations.transform(pivotVS);
                        SightTracker.recordMeasured(new Vec3(pivotVS.x, pivotVS.y, pivotVS.z));
                    });
                }
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
                    MuzzleTracker.record(mc.player.getUUID(), hand, muzzle);
                });
            }
        };
    }

    protected void afterGunPreRender(AzRendererPipelineContext<UUID, ItemStack> context) {
    }

    /**
     * X-axis rotation at full aim, in degrees. Lets guns whose models sit slightly
     * tilted be levelled while aiming; flip the sign if it tips the wrong way.
     */
    protected float aimPitchDegrees() {
        return 0.0f;
    }

    /** Vertical slide at full aim; override per gun if its sights need a different height. */
    protected float aimRaise() {
        return AIM_CENTER_Y;
    }

    /**
     * Depth slide at full aim. Negative pushes the gun away from the camera; use it
     * when a tall model clips the near plane while aimed.
     */
    protected float aimSetback() {
        return AIM_CENTER_Z;
    }

    /** Raises the first-person gun toward eye level while aiming down sights. */
    private void applyAimPose(AzRendererPipelineContext<UUID, ItemStack> context, ItemDisplayContext displayCtx) {
        if (displayCtx != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                && displayCtx != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }
        float aim = GunAimHandler.progress(Minecraft.getInstance().getFrameTime());
        if (aim <= 0f) {
            return;
        }
        // Vertical placement comes from the back_sight measurement when available;
        // aimRaise is only the fallback for guns without that bone.
        float raise = SightTracker.rest() != null ? 0.0f : aimRaise();
        context.poseStack().translate(0.0f, raise * aim, aimSetback() * aim);
        float pitch = aimPitchDegrees();
        if (pitch != 0.0f) {
            context.poseStack().mulPose(Axis.XP.rotationDegrees(pitch * aim));
        }
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
