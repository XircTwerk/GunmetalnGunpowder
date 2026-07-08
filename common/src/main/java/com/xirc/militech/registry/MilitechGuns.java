package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.common.item.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public interface MilitechGuns {
    DeferredRegister<Item> GUNS = DeferredRegister.create(Militech.MOD_ID, Registries.ITEM);

    RegistrySupplier<AbstractGunItem> BERETTA = GUNS.register("beretta",
            () -> new BerettaItem(settings()));
    RegistrySupplier<AbstractGunItem> ASSAULT_RIFLE = GUNS.register("assault_rifle",
            () -> new AssaultRifleItem(settings()));

    static Item.Properties settings() {
        return new Item.Properties().rarity(Rarity.RARE).stacksTo(1);
    }

    static void init() {
        GUNS.register();
    }
}
