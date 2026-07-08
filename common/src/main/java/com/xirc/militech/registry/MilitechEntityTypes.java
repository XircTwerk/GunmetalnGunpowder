package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.entity.projectile.BulletProjectile;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public interface MilitechEntityTypes {
    DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Militech.MOD_ID, Registries.ENTITY_TYPE);

    RegistrySupplier<EntityType<BulletProjectile>> BULLET = ENTITY_TYPES.register("bullet",
            () -> EntityType.Builder.<BulletProjectile>of(BulletProjectile::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build("bullet"));

    static void init() {
        ENTITY_TYPES.register();
    }
}
