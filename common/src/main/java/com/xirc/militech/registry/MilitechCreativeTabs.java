package com.xirc.militech.registry;

import com.xirc.militech.Militech;
import com.xirc.militech.common.data.gun.GunAssemblyRecipe;
import com.xirc.militech.common.data.gun.GunAssemblyRecipes;
import com.xirc.militech.common.item.BlueprintItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface MilitechCreativeTabs {
    DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Militech.MOD_ID, Registries.CREATIVE_MODE_TAB);

    RegistrySupplier<CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                    .title(Component.translatable("itemgroup.militech.main"))
                    .icon(() -> new ItemStack(MilitechGuns.BERETTA.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(MilitechGuns.BERETTA.get());
                        entries.accept(MilitechGuns.ASSAULT_RIFLE.get());
                        entries.accept(MilitechItems.GUN_BENCH.get());
                        entries.accept(MilitechItems.FRAME.get());
                        entries.accept(MilitechItems.BARREL.get());
                        entries.accept(MilitechItems.MECHANISM.get());
                        entries.accept(MilitechItems.COMPONENT.get());
                        entries.accept(MilitechItems.MAGAZINE.get());
                    })
                    .build());

    RegistrySupplier<CreativeModeTab> AMMO = CREATIVE_TABS.register("ammo",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
                    .title(Component.translatable("itemgroup.militech.ammo"))
                    .icon(() -> new ItemStack(MilitechItems.PISTOL_ROUND.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(MilitechItems.PISTOL_ROUND.get());
                        entries.accept(MilitechItems.RIFLE_ROUND.get());
                        entries.accept(MilitechItems.SHOTGUN_SHELL.get());
                        entries.accept(MilitechItems.PISTOL_BOX.get());
                        entries.accept(MilitechItems.RIFLE_BOX.get());
                        entries.accept(MilitechItems.SHOTGUN_BOX.get());
                    })
                    .build());

    RegistrySupplier<CreativeModeTab> BLUEPRINTS = CREATIVE_TABS.register("blueprints",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
                    .title(Component.translatable("itemgroup.militech.blueprints"))
                    .icon(() -> new ItemStack(MilitechItems.BLUEPRINT.get()))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(MilitechItems.BLUEPRINT.get());
                        for (GunAssemblyRecipe recipe : GunAssemblyRecipes.RECIPES) {
                            entries.accept(BlueprintItem.forGun(recipe.result().get()));
                        }
                    })
                    .build());

    static void init() {
        CREATIVE_TABS.register();
    }
}
