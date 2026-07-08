package com.xirc.militech.common.item;

/**
 * Category of gun part accepted by a gun bench assembly slot. Each bench slot is
 * typed to exactly one of these, and assembly recipes demand counts per type.
 */
public enum GunPartType {
    FRAME("frame"),
    BARREL("barrel"),
    MECHANISM("mechanism"),
    COMPONENT("component"),
    MAGAZINE("magazine");

    private final String id;

    GunPartType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
