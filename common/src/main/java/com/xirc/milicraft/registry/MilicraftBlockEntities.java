package com.xirc.milicraft.registry;

import com.xirc.milicraft.Milicraft;
import com.xirc.milicraft.common.block.GunBenchBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface MilicraftBlockEntities {
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Milicraft.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    RegistrySupplier<BlockEntityType<GunBenchBlockEntity>> GUN_BENCH = BLOCK_ENTITIES.register("gun_bench",
            () -> BlockEntityType.Builder.of(GunBenchBlockEntity::new, MilicraftBlocks.GUN_BENCH.get()).build(null));

    static void init() {
        BLOCK_ENTITIES.register();
    }
}
