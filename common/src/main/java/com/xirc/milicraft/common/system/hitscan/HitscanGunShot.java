package com.xirc.milicraft.common.system.hitscan;

import com.xirc.milicraft.common.system.block.BulletBlockDamage;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public final class HitscanGunShot {
    public static final float CLOSE_DAMAGE_MULT = 3.0f;
    public static final double BASELINE_DISTANCE = 8.0;
    private static final int MAX_TRANSPARENT_BLOCKS = 16;
    private static final double RAY_STEP = 0.05;

    private HitscanGunShot() {
    }

    public static float damageMultiplier(double distance, float range) {
        if (distance <= BASELINE_DISTANCE) {
            double t = distance / BASELINE_DISTANCE;
            return (float) (CLOSE_DAMAGE_MULT + (1.0 - CLOSE_DAMAGE_MULT) * t);
        }
        double zeroDamageDistance = Math.max(BASELINE_DISTANCE + 1.0, range);
        double t = Math.min(1.0, (distance - BASELINE_DISTANCE) / (zeroDamageDistance - BASELINE_DISTANCE));
        return (float) (1.0 - t);
    }

    public static void fire(LivingEntity user, float damage, float range, float knockback, int barrels, int pelletsPerBarrel, float spread, float blockDamage) {
        fire(user, damage, range, knockback, barrels, pelletsPerBarrel, spread, blockDamage, null);
    }

    public static void fire(LivingEntity user, float damage, float range, float knockback, int barrels, int pelletsPerBarrel, float spread, float blockDamage, Projectile projectile) {
        fire(user, damage, range, knockback, barrels, pelletsPerBarrel, spread, blockDamage, projectile, null);
    }

    public static void fire(LivingEntity user, float damage, float range, float knockback, int barrels, int pelletsPerBarrel, float spread, float blockDamage, Projectile projectile, Consumer<Vec3> onImpact) {
        Level world = user.level();
        int pellets = Math.max(1, pelletsPerBarrel);
        float pelletDamage = damage / pellets;
        float pelletBlockDamage = blockDamage / pellets;
        int totalPellets = Math.max(1, barrels) * pellets;
        for (int i = 0; i < totalPellets; i++) {
            Vec3 impact = fireRay(user, world, pelletDamage, range, knockback, spread, pelletBlockDamage, projectile);
            if (onImpact != null) onImpact.accept(impact);
        }
    }

    private static Vec3 fireRay(LivingEntity user, Level world, float pelletDamage, float range, float knockback, float spread, float pelletBlockDamage, Projectile projectile) {
        Vec3 eye = user.getEyePosition();
        RandomSource random = user.getRandom();
        Vec3 look = user.getViewVector(1.0f).add(
                (random.nextDouble() - 0.5) * spread,
                (random.nextDouble() - 0.5) * spread,
                (random.nextDouble() - 0.5) * spread
        ).normalize();
        Vec3 end = eye.add(look.scale(range));

        BlockTrace blockTrace = traceBlocks(user, world, eye, end, look, pelletBlockDamage, projectile);
        end = blockTrace.end;

        AABB searchBox = new AABB(eye, end).inflate(0.3);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                world, user, eye, end, searchBox,
                e -> e instanceof LivingEntity && e != user && e.isAlive());

        Vec3 impact = end;
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            impact = entityHit.getLocation();
            float dmg = pelletDamage * damageMultiplier(eye.distanceTo(impact), range);
            target.invulnerableTime = 0;
            boolean damaged = dmg > 0 && target.hurt(world.damageSources().mobAttack(user), dmg);
            if (damaged && knockback > 0) {
                Vec3 kb = target.position().subtract(user.position()).normalize();
                target.knockback(knockback, -kb.x, -kb.z);
            }
        } else if (blockTrace.hit != null) {
            activateBlock(world, blockTrace.hit, projectile);
            damageBlock(world, blockTrace.hit, pelletBlockDamage);
        }

        return impact;
    }

    private static BlockTrace traceBlocks(LivingEntity user, Level world, Vec3 start, Vec3 end, Vec3 direction, float blockDamage, Projectile projectile) {
        Vec3 rayStart = start;
        for (int i = 0; i < MAX_TRANSPARENT_BLOCKS; i++) {
            BlockHitResult hit = world.clip(new ClipContext(
                    rayStart, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));
            if (hit.getType() == HitResult.Type.MISS) {
                return new BlockTrace(end, null);
            }

            BlockPos pos = hit.getBlockPos();
            boolean activated = activateBlock(world, hit, projectile);
            if (activated && BulletBlockDamage.breaksInstantly(world, pos, world.getBlockState(pos))) {
                BulletBlockDamage.hitNextTick(world, pos, blockDamage);
                return new BlockTrace(hit.getLocation(), null);
            }

            if (BulletBlockDamage.breaksInstantly(world, pos, world.getBlockState(pos))
                    && damageBlock(world, hit, blockDamage)) {
                rayStart = hit.getLocation().add(direction.scale(RAY_STEP));
                continue;
            }

            return new BlockTrace(hit.getLocation(), hit);
        }
        return new BlockTrace(rayStart, null);
    }

    private static boolean damageBlock(Level world, BlockHitResult hit, float blockDamage) {
        BlockPos pos = hit.getBlockPos();
        return BulletBlockDamage.hit(world, pos, world.getBlockState(pos), blockDamage);
    }

    private static boolean activateBlock(Level world, BlockHitResult hit, Projectile projectile) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ButtonBlock button) {
            if (!state.getValue(ButtonBlock.POWERED)) {
                button.press(state, world, pos);
            }
            return true;
        }

        if (state.getBlock() instanceof BellBlock bell) {
            return bell.attemptToRing(world, pos, hit.getDirection());
        }

        if (projectile == null) {
            return false;
        }
        state.onProjectileHit(world, state, hit, projectile);
        return state.getBlock() instanceof TargetBlock;
    }

    private record BlockTrace(Vec3 end, BlockHitResult hit) {
    }
}
