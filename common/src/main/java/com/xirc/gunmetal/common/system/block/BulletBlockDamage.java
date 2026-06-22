package com.xirc.gunmetal.common.system.block;

import com.xirc.gunmetal.registry.GunmetalGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BulletBlockDamage {
    private static final int DAMAGE_TIMEOUT_TICKS = 100;
    private static final float BREAKAGE_SCALE = 0.125f;
    private static final float MIN_CHIPPED_BLAST_RESISTANCE = 2.0f;
    private static final float MAX_CHIPPED_BLAST_RESISTANCE = 4.0f;
    private static final Map<BlockKey, DamageState> DAMAGE = new HashMap<>();

    private BulletBlockDamage() {
    }

    public static boolean hit(Level level, BlockPos pos, BlockState state, float strength) {
        if (!(level instanceof ServerLevel serverLevel)
                || !serverLevel.getGameRules().getBoolean(GunmetalGameRules.BULLETS_BREAK_BLOCKS)
                || state.isAir()
                || strength <= 0) {
            return false;
        }

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) {
            return false;
        }

        if (breaksInstantly(level, pos, state)) {
            boolean destroyed = breakBlockAndDrop(serverLevel, pos);
            if (destroyed) {
                clear(serverLevel, pos);
            }
            return destroyed;
        }

        float blastResistance = state.getBlock().getExplosionResistance();
        if (!isChippable(state, blastResistance)) {
            return false;
        }

        cleanup(serverLevel);
        BlockKey key = new BlockKey(serverLevel.dimension(), pos.immutable());
        long now = serverLevel.getGameTime();
        DamageState current = DAMAGE.get(key);
        float previous = current != null && now - current.lastHit <= DAMAGE_TIMEOUT_TICKS ? current.damage : 0.0f;
        float damage = previous + strength * BREAKAGE_SCALE;
        float breakage = damage / Math.max(0.1f, hardness);

        if (breakage >= 1.0f) {
            DAMAGE.remove(key);
            clear(serverLevel, pos);
            return breakBlockAndDrop(serverLevel, pos);
        }

        DAMAGE.put(key, new DamageState(damage, now));
        serverLevel.destroyBlockProgress(breakerId(pos), pos, Mth.clamp((int) (breakage * 10.0f), 0, 9));
        return false;
    }

    public static boolean breaksInstantly(Level level, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getDestroySpeed(level, pos) >= 0 && !state.canOcclude();
    }

    private static boolean isChippable(BlockState state, float blastResistance) {
        return state.is(Blocks.STONE)
                || (blastResistance >= MIN_CHIPPED_BLAST_RESISTANCE && blastResistance <= MAX_CHIPPED_BLAST_RESISTANCE);
    }

    private static boolean breakBlockAndDrop(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        boolean removed = level.removeBlock(pos, false);
        if (removed) {
            Block.dropResources(state, level, pos, blockEntity);
            level.levelEvent(2001, pos, Block.getId(state));
        }
        return removed;
    }

    private static void clear(ServerLevel level, BlockPos pos) {
        DAMAGE.remove(new BlockKey(level.dimension(), pos.immutable()));
        level.destroyBlockProgress(breakerId(pos), pos, -1);
    }

    private static void cleanup(ServerLevel level) {
        long now = level.getGameTime();
        if (DAMAGE.size() < 1024) {
            return;
        }
        Iterator<Map.Entry<BlockKey, DamageState>> iterator = DAMAGE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockKey, DamageState> entry = iterator.next();
            if (entry.getKey().dimension.equals(level.dimension()) && now - entry.getValue().lastHit > DAMAGE_TIMEOUT_TICKS) {
                level.destroyBlockProgress(breakerId(entry.getKey().pos), entry.getKey().pos, -1);
                iterator.remove();
            }
        }
    }

    private static int breakerId(BlockPos pos) {
        return pos.hashCode();
    }

    private record BlockKey(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record DamageState(float damage, long lastHit) {
    }
}
