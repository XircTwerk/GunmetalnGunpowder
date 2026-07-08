package com.xirc.militech.common.item;

import com.xirc.militech.registry.MilitechItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A gun blueprint. The gun it documents is stored in NBT so one item covers every
 * gun; the tooltip names that gun. Currently informational - intended to gate gun
 * bench recipes once blueprint unlocking is added.
 */
public class BlueprintItem extends Item {
    public static final String GUN_ID = "Gun";

    public BlueprintItem(Properties settings) {
        super(settings);
    }

    public static ItemStack forGun(Item gun) {
        ItemStack stack = new ItemStack(MilitechItems.BLUEPRINT.get());
        stack.getOrCreateTag().putString(GUN_ID, BuiltInRegistries.ITEM.getKey(gun).toString());
        return stack;
    }

    /** The gun item this blueprint documents, or null for a blank blueprint. */
    @Nullable
    public static Item gunFor(ItemStack stack) {
        if (stack.getTag() == null || !stack.getTag().contains(GUN_ID)) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(stack.getTag().getString(GUN_ID));
        if (id == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item instanceof AbstractGunItem ? item : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        Item gun = gunFor(stack);
        if (gun != null) {
            tooltip.add(Component.translatable("tooltip.militech.blueprint.gun", gun.getDescription())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.militech.blueprint.blank").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, world, tooltip, context);
    }
}
