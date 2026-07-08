package com.xirc.militech.common.item;

/**
 * Ammo families shared between guns, ammo rounds, and ammo boxes.
 * <p>
 * A gun fires one {@link AmmoType}, the matching round item feeds it, and the
 * matching ammo box only accepts that round.
 */
public enum AmmoType {
    PISTOL("pistol"),
    RIFLE("rifle"),
    SHOTGUN("shotgun");

    private final String id;

    AmmoType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
