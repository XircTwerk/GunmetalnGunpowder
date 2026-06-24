package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.menu.AmmoBoxMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public interface GunmetalMenus {
    DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.MENU);

    RegistrySupplier<MenuType<AmmoBoxMenu>> AMMO_BOX = MENUS.register("ammo_box",
            () -> MenuRegistry.ofExtended(AmmoBoxMenu::new));

    static void init() {
        MENUS.register();
    }
}
