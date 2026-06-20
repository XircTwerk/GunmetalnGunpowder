package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.BerettaItem;
import com.xirc.gunmetal.common.item.BulletItem;
import com.xirc.gunmetal.common.item.PlaceholderGunItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class GunmetalItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> BULLET = ITEMS.register("bullet", () -> new BulletItem(settings()));
    public static final RegistrySupplier<Item> PLACEHOLDER_GUN = ITEMS.register("placeholder_gun", () -> new PlaceholderGunItem(settings().rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistrySupplier<Item> BERETTA = ITEMS.register("beretta", () -> new BerettaItem(settings().rarity(Rarity.RARE).stacksTo(1)));

    private GunmetalItems() {
    }

    private static Item.Properties settings() {
        return new Item.Properties();
    }

    public static void init() {
        ITEMS.register();
    }
}
