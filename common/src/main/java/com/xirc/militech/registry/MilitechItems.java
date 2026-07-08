package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.item.AmmoBoxItem;
import com.xirc.militech.common.item.AmmoItem;
import com.xirc.militech.common.item.AmmoType;
import com.xirc.militech.common.item.BlueprintItem;
import com.xirc.militech.common.item.GunPartItem;
import com.xirc.militech.common.item.GunPartType;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public interface MilitechItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(Militech.MOD_ID, Registries.ITEM);

    RegistrySupplier<Item> PISTOL_ROUND = ITEMS.register("pistol_round", () -> new AmmoItem(settings(), AmmoType.PISTOL));
    RegistrySupplier<Item> RIFLE_ROUND = ITEMS.register("rifle_round", () -> new AmmoItem(settings(), AmmoType.RIFLE));
    RegistrySupplier<Item> SHOTGUN_SHELL = ITEMS.register("shotgun_shell", () -> new AmmoItem(settings(), AmmoType.SHOTGUN));

    RegistrySupplier<Item> PISTOL_BOX = ITEMS.register("pistol_box", () -> new AmmoBoxItem(boxSettings(), AmmoType.PISTOL));
    RegistrySupplier<Item> RIFLE_BOX = ITEMS.register("rifle_box", () -> new AmmoBoxItem(boxSettings(), AmmoType.RIFLE));
    RegistrySupplier<Item> SHOTGUN_BOX = ITEMS.register("shotgun_box", () -> new AmmoBoxItem(boxSettings(), AmmoType.SHOTGUN));

    RegistrySupplier<Item> FRAME = ITEMS.register("frame", () -> new GunPartItem(settings(), GunPartType.FRAME));
    RegistrySupplier<Item> BARREL = ITEMS.register("barrel", () -> new GunPartItem(settings(), GunPartType.BARREL));
    RegistrySupplier<Item> MECHANISM = ITEMS.register("mechanism", () -> new GunPartItem(settings(), GunPartType.MECHANISM));
    RegistrySupplier<Item> COMPONENT = ITEMS.register("component", () -> new GunPartItem(settings(), GunPartType.COMPONENT));
    RegistrySupplier<Item> MAGAZINE = ITEMS.register("magazine", () -> new GunPartItem(settings(), GunPartType.MAGAZINE));

    RegistrySupplier<Item> GUN_BENCH = ITEMS.register("gun_bench", () -> new BlockItem(MilitechBlocks.GUN_BENCH.get(), settings()));

    RegistrySupplier<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new BlueprintItem(settings().stacksTo(1)));

    static Item partFor(GunPartType type) {
        return switch (type) {
            case FRAME -> FRAME.get();
            case BARREL -> BARREL.get();
            case MECHANISM -> MECHANISM.get();
            case COMPONENT -> COMPONENT.get();
            case MAGAZINE -> MAGAZINE.get();
        };
    }

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
