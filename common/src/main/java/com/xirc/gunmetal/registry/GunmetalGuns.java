package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import com.xirc.gunmetal.common.item.BerettaItem;
import com.xirc.gunmetal.common.item.PlaceholderGunItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public interface GunmetalGuns {
    DeferredRegister<Item> GUNS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.ITEM);

    RegistrySupplier<AbstractGunItem> BERETTA = GUNS.register("beretta",
            () -> new BerettaItem(settings()));

    static Item.Properties settings() {
        return new Item.Properties().rarity(Rarity.RARE).stacksTo(1);
    }

    static void init() {
        GUNS.register();
    }
}
