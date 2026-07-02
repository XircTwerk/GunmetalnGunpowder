package com.xirc.gunmetal.client.tracer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.xirc.gunmetal.common.entity.projectile.BulletProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class TracerRenderer {
    private static final double LENGTH = 1.2;
    // AbstractArrow applies 0.99 drag per tick; match it or the tip outruns the bullet.
    private static final double DRAG = 0.99;

    private TracerRenderer() {}

    public static void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        boolean any = false;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof BulletProjectile) { any = true; break; }
        }
        if (!any) return;

        Vec3 cam = camera.getPosition();
        Matrix4f mat = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Depth-tested so tracers don't draw through walls or the shooter in third person.
        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(4.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof BulletProjectile bullet)) continue;

            // Entity sync can't keep up at bullet speeds (clamped motion packets,
            // rubber-banding), so extrapolate from the spawn point along the launch
            // velocity instead of following the synced position. The entity itself
            // only gates despawn and colour.
            Vec3 velocity = bullet.getTracerVelocity();
            double speed = velocity.length();
            if (speed < 1e-3) continue;
            Vec3 dir = velocity.scale(1.0 / speed);

            double ticks = bullet.tickCount + partialTick;
            double travelled = speed * (1.0 - Math.pow(DRAG, ticks)) / (1.0 - DRAG);
            if (travelled < 1e-4) continue;

            Vec3 tip = bullet.getTracerSpawn().add(dir.scale(travelled));
            Vec3 tail = tip.subtract(dir.scale(Math.min(LENGTH, travelled)));

            int color = bullet.getTracerColor();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8)  & 0xFF) / 255f;
            float b = (color         & 0xFF) / 255f;

            // Tip (bright, at bullet) → tail (fades out).
            buffer.vertex(mat, (float)(tip.x - cam.x), (float)(tip.y - cam.y), (float)(tip.z - cam.z))
                    .color(r, g, b, 1.0f).endVertex();
            buffer.vertex(mat, (float)(tail.x - cam.x), (float)(tail.y - cam.y), (float)(tail.z - cam.z))
                    .color(r, g, b, 0.0f).endVertex();
        }

        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
