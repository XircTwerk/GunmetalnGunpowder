package com.xirc.milicraft.registry;

import com.xirc.milicraft.Milicraft;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public interface MilicraftSoundRegistry {
    DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Milicraft.MOD_ID, Registries.SOUND_EVENT);

    //placeholders until mr record adds em
    RegistrySupplier<SoundEvent> BULLET_RICOCHET = register("bulletricochet");
    RegistrySupplier<SoundEvent> BULLET_PENETRATE = register("bulletpenetrate");
    RegistrySupplier<SoundEvent> LOAD = register("reload");
    RegistrySupplier<SoundEvent> REVOLVER_FIRE = register("revolver_fire");
    RegistrySupplier<SoundEvent> BERETTA_M9_SHOT = register("beretta_m9_shot");
    RegistrySupplier<SoundEvent> ASSAULT_RIFLE_SHOT = register("assault_rifle_shot");
    RegistrySupplier<SoundEvent> WEAP_TRIGGER_HAMMER = register("weap_trigger_hammer");
    RegistrySupplier<SoundEvent> WEAP_BOLT_OUT = register("weap_bolt_out");
    RegistrySupplier<SoundEvent> WEAP_MAGDROP_PLASTIC = register("weap_magdrop_plastic");
    RegistrySupplier<SoundEvent> WEAP_MAGIN_PLASTIC = register("weap_magin_plastic");

    static RegistrySupplier<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Milicraft.id(name)));
    }

    static void init() {
        SOUND_EVENTS.register();
    }
}
