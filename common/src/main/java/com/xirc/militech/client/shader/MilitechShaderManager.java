package com.xirc.militech.client.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for all Militech post-processing effects.
 *
 * Depth handling:
 *   MC's main render target exposes its depth buffer as a regular GL texture
 *   via RenderTarget.depthBufferId. We bind that texture as "DiffuseDepthSampler"
 *   on every active processor before running shader passes.
 *
 *   This works because:
 *   - processAll() runs at TAIL of LevelRenderer.renderLevel() — all geometry is
 *     done, sky pixels have depth 1.0, world pixels have depth < 1.0.
 *   - During PostChain processing, RenderSystem.disableDepthTest() is called, so
 *     the depth buffer is not written to. The depth values we bind are stable.
 */
public class MilitechShaderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MilitechShaderManager.class);
    private static final MilitechShaderManager INSTANCE = new MilitechShaderManager();
    private final List<MilitechPostProcessor> processors = new ArrayList<>();

    private MilitechShaderManager() {}

    public static MilitechShaderManager getInstance() {
        return INSTANCE;
    }

    public void register(MilitechPostProcessor processor) {
        if (!processors.contains(processor)) {
            processors.add(processor);
        }
    }

    /**
     * Process all active shaders. Call this at the end of rendering
     * (TAIL of LevelRenderer.renderLevel).
     */
    public void processAll(PoseStack viewModelStack, float partialTick) {
        for (MilitechPostProcessor processor : processors) {
            if (!processor.isActive()) {
                continue;
            }

            try {
                processor.process(viewModelStack, partialTick);
            } catch (RuntimeException exception) {
                processor.setActive(false);
                LOGGER.error("Disabled failing shader {}", processor.getClass().getSimpleName(), exception);
            }
        }
    }

    public void resize(int width, int height) {
        processors.forEach(p -> p.resize(width, height));
    }

    public void reloadAll() {
        processors.forEach(MilitechPostProcessor::loadShader);
    }

    @SuppressWarnings("unchecked")
    public <T extends MilitechPostProcessor> T getProcessor(Class<T> clazz) {
        for (MilitechPostProcessor processor : processors) {
            if (clazz.isInstance(processor)) {
                return (T) processor;
            }
        }
        return null;
    }
}
