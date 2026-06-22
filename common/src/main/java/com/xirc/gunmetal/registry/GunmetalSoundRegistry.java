package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public interface GunmetalSoundRegistry {
    DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.SOUND_EVENT);

    //placeholders until mr record adds em
    RegistrySupplier<SoundEvent> BULLET_RICOCHET = register("bulletricochet");
    RegistrySupplier<SoundEvent> BULLET_PENETRATE = register("bulletpenetrate");
    RegistrySupplier<SoundEvent> LOAD = register("reload");
    RegistrySupplier<SoundEvent> REVOLVER_FIRE = register("revolver_fire");
    RegistrySupplier<SoundEvent> BERETTA_M9_SHOT = register("beretta_m9_shot");
    RegistrySupplier<SoundEvent> M16_SHOT = register("m16_shot");

    static RegistrySupplier<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Gunmetal.id(name)));
    }

    static void init() {
        SOUND_EVENTS.register();
    }
}
