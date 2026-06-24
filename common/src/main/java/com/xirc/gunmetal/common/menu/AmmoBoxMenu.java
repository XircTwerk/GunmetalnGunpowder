package com.xirc.gunmetal.common.menu;

import com.xirc.gunmetal.common.item.AmmoBoxItem;
import com.xirc.gunmetal.common.item.AmmoType;
import com.xirc.gunmetal.registry.GunmetalItems;
import com.xirc.gunmetal.registry.GunmetalMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 3-slot menu for an {@link AmmoBoxItem}. Each ammo slot only accepts the box's matching
 * round (which also blocks placing the box, or any other box, inside itself). Contents are
 * persisted back into the held box's NBT when the menu changes or closes.
 */
public class AmmoBoxMenu extends AbstractContainerMenu {
    private final Container container;
    private final AmmoType ammoType;
    private final ItemStack box;

    /** Client-side constructor: reads the box's ammo type from the sync buffer. */
    public AmmoBoxMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory, new SimpleContainer(AmmoBoxItem.SLOTS), buf.readEnum(AmmoType.class), ItemStack.EMPTY);
    }

    /** Server-side factory: backs the menu with the box's stored rounds. */
    public static AmmoBoxMenu server(int syncId, Inventory playerInventory, ItemStack box, AmmoType ammoType) {
        SimpleContainer container = new SimpleContainer(AmmoBoxItem.SLOTS);
        NonNullList<ItemStack> items = AmmoBoxItem.readItems(box);
        for (int i = 0; i < items.size(); i++) {
            container.setItem(i, items.get(i));
        }
        return new AmmoBoxMenu(syncId, playerInventory, container, ammoType, box);
    }

    private AmmoBoxMenu(int syncId, Inventory playerInventory, Container container, AmmoType ammoType, ItemStack box) {
        super(GunmetalMenus.AMMO_BOX.get(), syncId);
        checkContainerSize(container, AmmoBoxItem.SLOTS);
        this.container = container;
        this.ammoType = ammoType;
        this.box = box;
        container.startOpen(playerInventory.player);

        int ammoStart = (176 - AmmoBoxItem.SLOTS * 18) / 2;
        for (int i = 0; i < AmmoBoxItem.SLOTS; i++) {
            this.addSlot(new AmmoSlot(container, i, ammoStart + i * 18, 20, ammoType));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        save();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
        save();
    }

    private void save() {
        if (box.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(AmmoBoxItem.SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < AmmoBoxItem.SLOTS; i++) {
            items.set(i, container.getItem(i));
        }
        AmmoBoxItem.writeItems(box, items);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < AmmoBoxItem.SLOTS) {
                // ammo slot -> player inventory
                if (!this.moveItemStackTo(stack, AmmoBoxItem.SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, AmmoBoxItem.SLOTS, false)) {
                // player inventory -> ammo slots (mayPlace filter rejects non-matching items)
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private static class AmmoSlot extends Slot {
        private final AmmoType ammoType;

        AmmoSlot(Container container, int index, int x, int y, AmmoType ammoType) {
            super(container, index, x, y);
            this.ammoType = ammoType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == GunmetalItems.roundFor(ammoType);
        }
    }
}
