package com.xirc.militech.mixin_logic;

/** Injected onto RenderTarget to expose the depth buffer texture ID. */
public interface MilitechDepthHolder {
    int militech$getDepthTexId();
}
