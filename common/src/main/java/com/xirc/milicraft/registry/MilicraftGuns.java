package com.xirc.milicraft.registry;

import com.xirc.milicraft.Milicraft;
import com.xirc.milicraft.common.item.AbstractGunItem;
import com.xirc.milicraft.common.item.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public interface MilicraftGuns {
    DeferredRegister<Item> GUNS = DeferredRegister.create(Milicraft.MOD_ID, Registries.ITEM);

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
