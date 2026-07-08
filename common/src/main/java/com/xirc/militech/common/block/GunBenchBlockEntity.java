package com.xirc.militech.common.block;

import com.xirc.militech.registry.MilitechBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Persistent per-bench storage. Currently holds the blueprint sidebar; bench
 * upgrades will live here too.
 */
public class GunBenchBlockEntity extends BlockEntity {
    public static final int BLUEPRINT_SLOTS = 8;

    private final SimpleContainer blueprints = new SimpleContainer(BLUEPRINT_SLOTS);

    public GunBenchBlockEntity(BlockPos pos, BlockState state) {
        super(MilitechBlockEntities.GUN_BENCH.get(), pos, state);
        this.blueprints.addListener(container -> setChanged());
    }

    public SimpleContainer getBlueprints() {
        return blueprints;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.blueprints.fromTag(tag.getList("Blueprints", CompoundTag.TAG_COMPOUND));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Blueprints", this.blueprints.createTag());
    }
}
