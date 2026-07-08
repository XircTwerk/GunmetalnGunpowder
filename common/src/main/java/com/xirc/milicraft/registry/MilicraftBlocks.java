package com.xirc.milicraft.registry;

import com.xirc.milicraft.Milicraft;
import com.xirc.milicraft.common.block.GunBenchBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public interface MilicraftBlocks {
    DeferredRegister<Block> BLOCKS = DeferredRegister.create(Milicraft.MOD_ID, Registries.BLOCK);

    RegistrySupplier<Block> GUN_BENCH = BLOCKS.register("gun_bench",
            () -> new GunBenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    static void init() {
        BLOCKS.register();
    }
}
