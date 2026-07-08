package com.xirc.militech.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public final class MilitechRaycast {
    private MilitechRaycast() {
    }

    public static BlockHitResult genericBlockRaycast(Level world, Entity entity, double range, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) {
        Vec3 eyePos = entity.getEyePosition();
        return world.clip(new ClipContext(
                eyePos,
                eyePos.add(entity.getLookAngle().scale(range)),
                shapeType,
                fluidHandling,
                entity));
    }

    public static HitResult raycastAll(Entity entity, Vec3 start, Vec3 end, ClipContext.Fluid fluidHandling) {
        return raycastAll(entity, start, end, fluidHandling, null);
    }

    public static HitResult raycastAll(Entity entity, Vec3 start, Vec3 end, ClipContext.Fluid fluidHandling, Predicate<Entity> entityPredicate) {
        Level world = entity.level();
        double rangeSquared = start.distanceToSqr(end);

        Predicate<Entity> combined = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> !e.isPassengerOfSameVehicle(entity));
        if (entityPredicate != null) {
            combined = combined.and(entityPredicate);
        }

        EntityHitResult eHit = ProjectileUtil.getEntityHitResult(entity, start, end,
                entity.getBoundingBox().inflate(rangeSquared),
                combined,
                rangeSquared);
        boolean entityHit = eHit != null && eHit.getType() == HitResult.Type.ENTITY;
        HitResult bHit = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, fluidHandling, entity));

        Vec3 blockPos = bHit.getLocation();

        if (entityHit) {
            Vec3 entityPos = eHit.getLocation();
            if (blockPos.distanceToSqr(start) > entityPos.distanceToSqr(start)) {
                return eHit;
            } else {
                return bHit;
            }
        }

        return bHit;
    }
}
