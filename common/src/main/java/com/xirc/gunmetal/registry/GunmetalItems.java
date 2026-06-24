package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AmmoBoxItem;
import com.xirc.gunmetal.common.item.AmmoItem;
import com.xirc.gunmetal.common.item.AmmoType;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public interface GunmetalItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.ITEM);

    RegistrySupplier<Item> PISTOL_ROUND = ITEMS.register("pistol_round", () -> new AmmoItem(settings(), AmmoType.PISTOL));
    RegistrySupplier<Item> RIFLE_ROUND = ITEMS.register("rifle_round", () -> new AmmoItem(settings(), AmmoType.RIFLE));
    RegistrySupplier<Item> SHOTGUN_SHELL = ITEMS.register("shotgun_shell", () -> new AmmoItem(settings(), AmmoType.SHOTGUN));

    RegistrySupplier<Item> PISTOL_BOX = ITEMS.register("pistol_box", () -> new AmmoBoxItem(boxSettings(), AmmoType.PISTOL));
    RegistrySupplier<Item> RIFLE_BOX = ITEMS.register("rifle_box", () -> new AmmoBoxItem(boxSettings(), AmmoType.RIFLE));
    RegistrySupplier<Item> SHOTGUN_BOX = ITEMS.register("shotgun_box", () -> new AmmoBoxItem(boxSettings(), AmmoType.SHOTGUN));

    static Item.Properties settings() {
        return new Item.Properties();
    }

    static Item.Properties boxSettings() {
        return new Item.Properties().stacksTo(1);
    }

    static Item roundFor(AmmoType type) {
        return switch (type) {
            case PISTOL -> PISTOL_ROUND.get();
            case RIFLE -> RIFLE_ROUND.get();
            case SHOTGUN -> SHOTGUN_SHELL.get();
        };
    }

    static Item boxFor(AmmoType type) {
        return switch (type) {
            case PISTOL -> PISTOL_BOX.get();
            case RIFLE -> RIFLE_BOX.get();
            case SHOTGUN -> SHOTGUN_BOX.get();
        };
    }

    static void init() {
        ITEMS.register();
    }
}
