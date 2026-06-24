package com.xirc.gunmetal.datagen.providers.assets;

import com.xirc.gunmetal.registry.GunmetalItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

public class GunmetalModelProvider extends FabricModelProvider {

    public GunmetalModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
        // No blocks to generate yet.
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        // Flat ammo rounds and ammo boxes (layer0 -> textures/item/<name>.png).
        // Guns use hand-authored builtin/entity models and are not generated here.
        generator.generateFlatItem(GunmetalItems.PISTOL_ROUND.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(GunmetalItems.RIFLE_ROUND.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(GunmetalItems.SHOTGUN_SHELL.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(GunmetalItems.PISTOL_BOX.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(GunmetalItems.RIFLE_BOX.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(GunmetalItems.SHOTGUN_BOX.get(), ModelTemplates.FLAT_ITEM);
    }
}
