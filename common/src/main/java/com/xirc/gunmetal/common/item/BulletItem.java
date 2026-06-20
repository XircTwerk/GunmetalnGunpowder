package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.registry.GunmetalItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BulletItem extends Item {
    public BulletItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag itemData = stack.getTag();

        if (itemData != null && itemData.contains("Caliber")) {
            tooltip.add(Component.translatable("gunmetal.bullet.caliber")
                    .append(Component.literal(" " + itemData.getFloat("Caliber") + "mm").withStyle(ChatFormatting.YELLOW)));
        }
        super.appendHoverText(stack, world, tooltip, context);
    }

    public static ItemStack ofCaliber(float caliber) {
        ItemStack s = new ItemStack(GunmetalItems.BULLET.get());
        CompoundTag nbt = s.getOrCreateTag();
        nbt.putFloat("Caliber", caliber);
        return s;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack s = new ItemStack(this);
        CompoundTag nbt = s.getOrCreateTag();
        nbt.putFloat("Caliber", 9);
        return s;
    }
}
