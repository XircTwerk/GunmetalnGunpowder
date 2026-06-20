package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public final class GunmetalSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BULLET_RICOCHET = register("bulletricochet");
    public static final RegistrySupplier<SoundEvent> BULLET_PENETRATE = register("bulletpenetrate");
    public static final RegistrySupplier<SoundEvent> LOAD = register("reload");
    public static final RegistrySupplier<SoundEvent> REVOLVER_FIRE = register("revolver_fire");

    private GunmetalSoundEvents() {
    }

    private static RegistrySupplier<SoundEvent> register(String name) {
        SoundEvent event = SoundEvent.createVariableRangeEvent(Gunmetal.id(name));
        return SOUND_EVENTS.register(event.getLocation().getPath(), () -> event);
    }

    public static void init() {
        SOUND_EVENTS.register();
    }
}
