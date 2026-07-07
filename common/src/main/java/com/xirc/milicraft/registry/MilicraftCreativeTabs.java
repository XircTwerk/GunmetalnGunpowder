package com.xirc.milicraft.registry;

import com.xirc.milicraft.Milicraft;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface MilicraftCreativeTabs {
    DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Milicraft.MOD_ID, Registries.CREATIVE_MODE_TAB);

    RegistrySupplier<CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                    .title(Component.translatable("itemgroup.milicraft.main"))
                    .icon(() -> new ItemStack(MilicraftGuns.BERETTA.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(MilicraftGuns.BERETTA.get());
                        entries.accept(MilicraftGuns.ASSAULT_RIFLE.get());
                        entries.accept(MilicraftItems.PISTOL_ROUND.get());
                        entries.accept(MilicraftItems.RIFLE_ROUND.get());
                        entries.accept(MilicraftItems.SHOTGUN_SHELL.get());
                        entries.accept(MilicraftItems.PISTOL_BOX.get());
                        entries.accept(MilicraftItems.RIFLE_BOX.get());
                        entries.accept(MilicraftItems.SHOTGUN_BOX.get());
                    })
                    .build());

    static void init() {
        CREATIVE_TABS.register();
    }
}
