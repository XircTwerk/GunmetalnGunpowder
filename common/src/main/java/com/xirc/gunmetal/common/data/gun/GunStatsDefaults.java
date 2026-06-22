package com.xirc.gunmetal.common.data.gun;

import com.xirc.gunmetal.Gunmetal;
import net.minecraft.resources.ResourceLocation;

public final class GunStatsDefaults {
    public static final ResourceLocation PLACEHOLDER_GUN_ID = Gunmetal.id("placeholder_gun");
    public static final ResourceLocation BERETTA_ID = Gunmetal.id("beretta");
    public static final ResourceLocation M16_ID = Gunmetal.id("m16");

    public static final GunStats PLACEHOLDER_GUN = new GunStats(
            6,
            5.0f,
            80.0f,
            0.25f,
            1,
            8,
            0.3f,
            9.0f,
            10.0f,
            2,
            10,
            20,
            60,
            10);

    public static final GunStats BERETTA = new GunStats(
            15,
            4.0f,
            60.0f,
            0.15f,
            1,
            1,
            0.0f,
            9.0f,
            19.0f,
            1,
            0,
            1,
            34,
            34);

    public static final GunStats M16 = new GunStats(
            20,
            6.0f,
            90.0f,
            0.2f,
            1,
            1,
            0.015f,
            5.56f,
            45.0f,
            1,
            0,
            3,
            29,
            29);

    private GunStatsDefaults() {
    }
}
