package com.xirc.militech.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.xirc.militech.mixin_logic.MilitechDepthHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Base class for full-screen post-processing shader effects, driven from
 * MilitechShaderManager at the tail of level rendering.
 */
public abstract class MilitechPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MilitechPostProcessor.class);
    protected static final Minecraft MC = Minecraft.getInstance();

    public static int getMainDepthTexId() {
        return ((MilitechDepthHolder) MC.getMainRenderTarget()).militech$getDepthTexId();
    }

    protected PostChain shaderEffect;
    protected EffectInstance[] effects;

    private boolean initialized = false;
    private boolean active = false;
    protected double time = 0.0;
    /** Wall-clock seconds elapsed since the previous process() call, updated each frame. */
    protected float frameDeltaSeconds = 0f;
    private long lastFrameNanos = System.nanoTime();

    /**
     * @return The shader effect ID (e.g., "militech:my_effect" -> "militech:shaders/post/my_effect.json")
     */
    public abstract ResourceLocation getShaderEffectId();

    /**
     * Called once when the processor is first initialized
     */
    public void init() {
        loadShader();

        initialized = true;
    }

    /**
     * Load or reload the shader from resources
     */
    public final void loadShader() {
        if (shaderEffect != null) {
            shaderEffect.close();
            shaderEffect = null;
        }

        try {
            ResourceLocation id = getShaderEffectId();
            ResourceLocation file = new ResourceLocation(
                    id.getNamespace(),
                    "shaders/post/" + id.getPath() + ".json"
            );

            LOGGER.debug("Loading shader from: {}", file);

            shaderEffect = new PostChain(
                    MC.getTextureManager(),
                    MC.getResourceManager(),
                    MC.getMainRenderTarget(),
                    file
            );

            shaderEffect.resize(MC.getWindow().getScreenWidth(), MC.getWindow().getScreenHeight());

            // PostChain.passes is private with no accessor in 1.20.1's mappings.
            var passesField = PostChain.class.getDeclaredField("passes");
            passesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            var passesList = (List<PostPass>) passesField.get(shaderEffect);
            effects = new EffectInstance[passesList.size()];
            for (int i = 0; i < passesList.size(); i++) {
                effects[i] = passesList.get(i).getEffect();
            }

            LOGGER.debug("Shader loaded successfully! Effects count: {}", effects.length);

        } catch (Exception ex) {
            LOGGER.error("Failed to load shader: {}", getShaderEffectId(), ex);
        }
    }

    /**
     * Bind a frozen depth texture as the DiffuseDepthSampler for all passes.
     * Called every frame before processing so DiffuseDepthSampler is always current.
     */
    public final void bindDepthTexture(int texId) {
        if (effects == null || texId == 0) return;
        for (EffectInstance effect : effects) {
            effect.setSampler("DiffuseDepthSampler", () -> texId);
        }
    }

    /**
     * Handle window resize
     */
    public void resize(int width, int height) {
        if (shaderEffect != null) {
            shaderEffect.resize(width, height);
        }
    }

    /**
     * Apply common uniforms (camera position, matrices, time, etc.)
     */
    protected void applyCommonUniforms(PoseStack viewModelStack) {
        if (effects == null) return;

        for (EffectInstance effect : effects) {
            Uniform timeUniform = effect.getUniform("Time");
            if (timeUniform != null) {
                timeUniform.set((float) time);
            }

            Uniform camPosUniform = effect.getUniform("CameraPosition");
            if (camPosUniform != null) {
                Vector3f pos = MC.gameRenderer.getMainCamera().getPosition().toVector3f();
                camPosUniform.set(pos);
            }

            Uniform invViewMatUniform = effect.getUniform("InverseViewMatrix");
            if (invViewMatUniform != null) {
                Matrix4f invViewMat = new Matrix4f(viewModelStack.last().pose());
                invViewMat.invert();
                invViewMatUniform.set(invViewMat);
            }

            Uniform invProjMatUniform = effect.getUniform("InverseProjectionMatrix");
            if (invProjMatUniform != null) {
                Matrix4f invProjMat = new Matrix4f(RenderSystem.getProjectionMatrix());
                invProjMat.invert();
                invProjMatUniform.set(invProjMat);
            }

            Uniform fovUniform = effect.getUniform("FOV");
            if (fovUniform != null) {
                float fov = (float) Math.toRadians(MC.options.fov().get());
                fovUniform.set(fov);
            }
        }
    }

    /**
     * Main render method - called every frame
     * @param partialTick the render partial tick, forwarded from LevelRenderer.renderLevel
     */
    public final void process(PoseStack viewModelStack, float partialTick) {
        if (!active) return;

        if (!initialized) {
            LOGGER.debug("Initializing shader on first process call");
            init();
        }

        if (shaderEffect == null) {
            LOGGER.error("Shader effect is null, cannot process!");
            return;
        }

        bindDepthTexture(getMainDepthTexId());

        long now = System.nanoTime();
        frameDeltaSeconds = (now - lastFrameNanos) / 1_000_000_000f;
        lastFrameNanos = now;
        time += frameDeltaSeconds;

        applyCommonUniforms(viewModelStack);
        beforeProcess(viewModelStack);

        if (!active) return;

        // Match the GL state vanilla uses for its own post effects.
        // Depth testing and blending left over from world rendering will
        // either kill the fullscreen quad (depth fail) or blend it against
        // black — both produce a black screen.
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.resetTextureMatrix();

        shaderEffect.process(partialTick);

        MC.getMainRenderTarget().bindWrite(true);
        RenderSystem.enableDepthTest();

        afterProcess();
    }

    /**
     * Override to set custom uniforms before rendering
     */
    protected void beforeProcess(PoseStack viewModelStack) {}

    /**
     * Override to clean up after rendering
     */
    protected void afterProcess() {}

    /**
     * Activate this effect
     */
    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            time = 0.0;
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
