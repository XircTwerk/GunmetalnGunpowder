package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.BulletItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public interface GunmetalItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.ITEM);

    RegistrySupplier<Item> BULLET = ITEMS.register("bullet", () -> new BulletItem(settings()));

    static Item.Properties settings() {
        return new Item.Properties();
    }

    static void init() {
        ITEMS.register();
    }
}
