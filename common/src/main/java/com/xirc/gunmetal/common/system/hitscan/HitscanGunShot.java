package com.xirc.gunmetal.common.system.hitscan;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class HitscanGunShot {
    public static final float CLOSE_DAMAGE_MULT = 3.0f;
    public static final double BASELINE_DISTANCE = 8.0;

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

    public static void fire(LivingEntity user, float damage, float range, float knockback, int barrels, int pelletsPerBarrel, float spread) {
        Level world = user.level();
        int pellets = Math.max(1, pelletsPerBarrel);
        float pelletDamage = damage / pellets;
        int totalPellets = Math.max(1, barrels) * pellets;
        for (int i = 0; i < totalPellets; i++) {
            fireRay(user, world, pelletDamage, range, knockback, spread);
        }
    }

    private static void fireRay(LivingEntity user, Level world, float pelletDamage, float range, float knockback, float spread) {
        Vec3 eye = user.getEyePosition();
        RandomSource random = user.getRandom();
        Vec3 look = user.getViewVector(1.0f).add(
                (random.nextDouble() - 0.5) * spread,
                (random.nextDouble() - 0.5) * spread,
                (random.nextDouble() - 0.5) * spread
        ).normalize();
        Vec3 end = eye.add(look.scale(range));

        BlockHitResult blockHit = world.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

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
        }

    }
}
