package com.xirc.milicraft.registry;

import com.xirc.milicraft.Milicraft;
import com.xirc.milicraft.common.data.gun.GunAssemblyRecipe;
import com.xirc.milicraft.common.data.gun.GunAssemblyRecipes;
import com.xirc.milicraft.common.item.BlueprintItem;
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
                        entries.accept(MilicraftItems.GUN_BENCH.get());
                        entries.accept(MilicraftItems.FRAME.get());
                        entries.accept(MilicraftItems.BARREL.get());
                        entries.accept(MilicraftItems.MECHANISM.get());
                        entries.accept(MilicraftItems.COMPONENT.get());
                        entries.accept(MilicraftItems.MAGAZINE.get());
                    })
                    .build());

    RegistrySupplier<CreativeModeTab> AMMO = CREATIVE_TABS.register("ammo",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
                    .title(Component.translatable("itemgroup.milicraft.ammo"))
                    .icon(() -> new ItemStack(MilicraftItems.PISTOL_ROUND.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(MilicraftItems.PISTOL_ROUND.get());
                        entries.accept(MilicraftItems.RIFLE_ROUND.get());
                        entries.accept(MilicraftItems.SHOTGUN_SHELL.get());
                        entries.accept(MilicraftItems.PISTOL_BOX.get());
                        entries.accept(MilicraftItems.RIFLE_BOX.get());
                        entries.accept(MilicraftItems.SHOTGUN_BOX.get());
                    })
                    .build());

    RegistrySupplier<CreativeModeTab> BLUEPRINTS = CREATIVE_TABS.register("blueprints",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
                    .title(Component.translatable("itemgroup.milicraft.blueprints"))
                    .icon(() -> new ItemStack(MilicraftItems.BLUEPRINT.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(MilicraftItems.BLUEPRINT.get());
                        for (GunAssemblyRecipe recipe : GunAssemblyRecipes.RECIPES) {
                            entries.accept(BlueprintItem.forGun(recipe.result().get()));
                        }
                    })
                    .build());

    static void init() {
        CREATIVE_TABS.register();
    }
}
