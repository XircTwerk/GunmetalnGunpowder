package com.xirc.militech.common.item;

import com.xirc.militech.common.menu.AmmoBoxMenu;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A container item that holds rounds of a single {@link AmmoType}.
 * <p>
 * Right-clicking opens a {@link AmmoBoxMenu} with {@link #SLOTS} slots that only accept
 * the matching round. Contents live in the stack's {@code Items} NBT list so guns can
 * pull from boxes carried in the player inventory during reload.
 */
public class AmmoBoxItem extends Item {
    public static final int SLOTS = 3;
    private static final String ITEMS_KEY = "Items";

    private final AmmoType ammoType;

    public AmmoBoxItem(Properties settings, AmmoType ammoType) {
        super(settings);
        this.ammoType = ammoType;
    }

    public AmmoType getAmmoType() {
        return ammoType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide && user instanceof ServerPlayer serverPlayer) {
            openMenu(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    private void openMenu(ServerPlayer player, ItemStack box) {
        MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider() {
            @Override
            public void saveExtraData(FriendlyByteBuf buf) {
                buf.writeEnum(ammoType);
            }

            @Override
            public Component getDisplayName() {
                return box.getHoverName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player p) {
                return AmmoBoxMenu.server(syncId, inventory, box, ammoType);
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("tooltip.militech.ammo_box.rounds")
                .append(Component.literal(" " + countRounds(stack)).withStyle(ChatFormatting.YELLOW)));
        super.appendHoverText(stack, world, tooltip, context);
    }

    /** Reads the box's stored rounds as a fixed-size list. */
    public static NonNullList<ItemStack> readItems(ItemStack box) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        CompoundTag tag = box.getTag();
        if (tag != null && tag.contains(ITEMS_KEY)) {
            ContainerHelper.loadAllItems(tag, items);
        }
        return items;
    }

    /** Writes the rounds back into the box's NBT. */
    public static void writeItems(ItemStack box, NonNullList<ItemStack> items) {
        ContainerHelper.saveAllItems(box.getOrCreateTag(), items);
    }

    /** Total rounds currently stored in this box. */
    public int countRounds(ItemStack box) {
        int count = 0;
        for (ItemStack s : readItems(box)) {
            count += s.getCount();
        }
        return count;
    }

    /**
     * Removes up to {@code amount} rounds from the box, writing the result back.
     *
     * @return the number of rounds actually removed
     */
    public int consumeRounds(ItemStack box, int amount) {
        if (amount <= 0) {
            return 0;
        }
        NonNullList<ItemStack> items = readItems(box);
        int consumed = 0;
        for (int i = 0; i < items.size(); i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) {
                continue;
            }
            int take = Math.min(amount - consumed, s.getCount());
            s.shrink(take);
            consumed += take;
            if (consumed >= amount) {
                break;
            }
        }
        if (consumed > 0) {
            writeItems(box, items);
        }
        return consumed;
    }
}
