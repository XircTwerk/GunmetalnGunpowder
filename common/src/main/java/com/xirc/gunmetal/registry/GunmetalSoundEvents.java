package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public interface GunmetalSoundEvents {
    DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.SOUND_EVENT);

    RegistrySupplier<SoundEvent> BULLET_RICOCHET = register("bulletricochet");
    RegistrySupplier<SoundEvent> BULLET_PENETRATE = register("bulletpenetrate");
    RegistrySupplier<SoundEvent> LOAD = register("reload");
    RegistrySupplier<SoundEvent> REVOLVER_FIRE = register("revolver_fire");

    static RegistrySupplier<SoundEvent> register(String name) {
        SoundEvent event = SoundEvent.createVariableRangeEvent(Gunmetal.id(name));
        return SOUND_EVENTS.register(event.getLocation().getPath(), () -> event);
    }

    static void init() {
        SOUND_EVENTS.register();
    }
}
