package com.xirc.gunmetal.registry;

import com.xirc.gunmetal.Gunmetal;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface GunmetalCreativeTabs {
    DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Gunmetal.MOD_ID, Registries.CREATIVE_MODE_TAB);

    RegistrySupplier<CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                    .title(Component.translatable("itemgroup.gunmetal.main"))
                    .icon(() -> new ItemStack(GunmetalGuns.BERETTA.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(GunmetalGuns.BERETTA.get());
                    })
                    .build());

    static void init() {
        CREATIVE_TABS.register();
    }
}
