package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.item.AbstractMeleeItem;
import com.xirc.militech.common.item.KnifeItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public interface MilitechMelee {
    DeferredRegister<Item> MELEE = DeferredRegister.create(Militech.MOD_ID, Registries.ITEM);

    RegistrySupplier<AbstractMeleeItem> KNIFE = MELEE.register("knife",
            () -> new KnifeItem(settings()));

    static Item.Properties settings() {
        return new Item.Properties().rarity(Rarity.RARE).stacksTo(1);
    }

    static void init() {
        MELEE.register();
    }
}
