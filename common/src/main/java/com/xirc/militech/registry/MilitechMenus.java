package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.menu.AmmoBoxMenu;
import com.xirc.militech.common.menu.GunBenchMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public interface MilitechMenus {
    DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Militech.MOD_ID, Registries.MENU);

    RegistrySupplier<MenuType<AmmoBoxMenu>> AMMO_BOX = MENUS.register("ammo_box",
            () -> MenuRegistry.ofExtended(AmmoBoxMenu::new));

    RegistrySupplier<MenuType<GunBenchMenu>> GUN_BENCH = MENUS.register("gun_bench",
            () -> new MenuType<>(GunBenchMenu::new, FeatureFlags.VANILLA_SET));

    static void init() {
        MENUS.register();
    }
}
