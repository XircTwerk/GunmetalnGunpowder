package com.xirc.gunmetal.common.entity.projectile;

import com.xirc.gunmetal.registry.GunmetalEntityTypes;
import com.xirc.gunmetal.registry.GunmetalSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BulletProjectile extends AbstractArrow {
    private int stunTicks;
    private float damage;
    private float mass;

    private static final EntityDataAccessor<Float> CALIBER = SynchedEntityData.defineId(BulletProjectile.class, EntityDataSerializers.FLOAT);

    public BulletProjectile(EntityType<? extends BulletProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public BulletProjectile(Level world) {
        super(GunmetalEntityTypes.BULLET.get(), world);
    }

    public BulletProjectile(Level world, LivingEntity owner, float caliber, float length, int stunTicks, float damage) {
        super(GunmetalEntityTypes.BULLET.get(), owner, world);

        setCaliber(caliber);
        this.stunTicks = stunTicks;
        this.damage = damage;
        this.mass = (length * caliber * caliber * Mth.PI) * 0.000000013f;

        setSoundEvent(GunmetalSoundEvents.BULLET_RICOCHET.get());
    }

    public void setCaliber(float cal) {
        entityData.set(CALIBER, cal);
    }

    public float getCaliber() {
        return entityData.get(CALIBER);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CALIBER, 9f);
        super.defineSynchedData();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();

        if (type == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitResult);
            level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = level().getBlockState(blockPos);
            if (blockState.isAir()) {
                return;
            }

            Vec3i intNormal = blockHitResult.getDirection().getNormal();
            Vec3 normal = new Vec3(intNormal.getX(), intNormal.getY(), intNormal.getZ());
            Vec3 impactVec = getDeltaMovement();

            double impactAngleRad = Math.acos(normal.dot(impactVec.normalize())) - Math.PI / 2.0;
            double impactAngleDeg = Math.toDegrees(impactAngleRad);

            double kineticEnergy = mass * impactVec.lengthSqr() / 2;
            double hardness = blockState.getBlock().defaultDestroyTime();
            if (hardness < 0) {
                hardness = Double.MAX_VALUE;
            }

            double penAngle = 45.0 + hardness * 5;
            boolean lowEnergy = kineticEnergy < 0.001;
            if (impactAngleDeg > penAngle || lowEnergy) {
                boolean through = hardness <= 1.0;
                if (lowEnergy || !through) {
                    onHitBlock(blockHitResult);
                    level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, blockState));

                    spawnImpactParticles();

                    discard();
                } else if (!level().isClientSide()) {
                    playServerSound(GunmetalSoundEvents.BULLET_PENETRATE.get(), position());
                    spawnImpactParticles();
                }
            } else {
                setDeltaMovement(impactVec.add(normal).scale(0.5 / hardness));
                if (!level().isClientSide()) {
                    playServerSound(GunmetalSoundEvents.BULLET_RICOCHET.get(), position());
                    spawnImpactParticles();
                }
            }
        }
    }

    @Override
    public void setDeltaMovement(Vec3 velocity) {
        super.setDeltaMovement(velocity);
        hurtMarked = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity living) {
            if (!level().isClientSide()) {
                Entity owner = getOwner();
                if (damage > 0) {
                    DamageSource thrown = level().damageSources().thrown(this, owner);
                    living.invulnerableTime = 0;
                    living.hurt(thrown, damage);
                }
                playServerSound(GunmetalSoundEvents.BULLET_PENETRATE.get(), position());
                spawnImpactParticles();

                discard();
            }
        } else {
            super.onHitEntity(entityHitResult);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel && !inGround) {
            Vec3 velocity = getDeltaMovement();
            double distance = velocity.length();

            if (distance > 0.1) {
                Vec3 start = position().subtract(velocity);
                Vec3 end = position();
                int particleCount = Math.max(1, (int) (distance * 4));

                for (int i = 0; i < particleCount; i++) {
                    double t = (double) i / particleCount;
                    Vec3 pos = start.lerp(end, t);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.02, 0.02, 0.02, 0.0);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("StunTicks", stunTicks);
        nbt.putFloat("Mass", mass);
        nbt.putFloat("Damage", damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        stunTicks = nbt.getInt("StunTicks");
        mass = nbt.getFloat("Mass");
        damage = nbt.getFloat("Damage");
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    private void spawnImpactParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
    }

    private void playServerSound(SoundEvent sound, Vec3 pos) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, 1, 1);
        }
    }
}
