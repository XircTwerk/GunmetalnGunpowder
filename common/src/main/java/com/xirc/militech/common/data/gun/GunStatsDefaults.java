package com.xirc.militech.common.data.gun;

import com.xirc.militech.Militech;
import net.minecraft.resources.ResourceLocation;

public final class GunStatsDefaults {
    public static final ResourceLocation BERETTA_ID = Militech.id("beretta");
    public static final ResourceLocation ASSAULT_RIFLE_ID = Militech.id("assault_rifle");

    public static final GunStats BERETTA = new GunStats(
            15,
            4.0f,
            60.0f,
            0.15f,
            1,
            1,
            0.15f,
            9.0f,
            19.0f,
            1,
            0,
            1,
            34,
            34);

    public static final GunStats ASSAULT_RIFLE = new GunStats(
            20,
            6.0f,
            90.0f,
            0.2f,
            1,
            1,
            0.05f,
            5.56f,
            45.0f,
            1,
            2,
            2,
            29,
            29);

    private GunStatsDefaults() {
    }
}
