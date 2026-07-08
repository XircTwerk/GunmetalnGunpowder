package com.xirc.milicraft.common.menu;

import com.xirc.milicraft.common.block.GunBenchBlockEntity;
import com.xirc.milicraft.common.data.gun.GunAssemblyRecipe;
import com.xirc.milicraft.common.data.gun.GunAssemblyRecipes;
import com.xirc.milicraft.common.item.BlueprintItem;
import com.xirc.milicraft.common.item.GunPartItem;
import com.xirc.milicraft.common.item.GunPartType;
import com.xirc.milicraft.registry.MilicraftBlocks;
import com.xirc.milicraft.registry.MilicraftMenus;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gun bench assembly menu. A 4x4 shapeless part grid plus a crafting-style result
 * slot, and a persistent blueprint sidebar backed by the bench's block entity.
 * Assembly only produces a gun whose blueprint sits in the sidebar; parts left in
 * the grid are returned on close, blueprints stay in the bench.
 */
public class GunBenchMenu extends AbstractContainerMenu {
    public static final int GRID_COLS = 4;
    public static final int GRID_ROWS = 3;
    public static final int GRID_SLOTS = GRID_COLS * GRID_ROWS;
    public static final int RESULT_SLOT = GRID_SLOTS;
    public static final int BLUEPRINT_START = RESULT_SLOT + 1;
    public static final int INV_START = BLUEPRINT_START + GunBenchBlockEntity.BLUEPRINT_SLOTS;
    /** Blueprint slots usable now; bench upgrades will add capacity in blocks of 8. */
    public static final int UNLOCKED_BLUEPRINT_SLOTS = 8;

    // Grid spaced out at a 22px pitch, shifted left of the preview panel.
    private static final int GRID_X = 56;
    private static final int GRID_Y = 40;
    private static final int GRID_PITCH = 22;
    private static final int RESULT_X = 150;
    private static final int RESULT_Y = 62;
    // Blueprint sidebar hangs off the left edge of the panel, 2 columns x 4 rows.
    private static final int BLUEPRINT_X = -44;
    private static final int BLUEPRINT_Y = 28;

    private final SimpleContainer parts = new SimpleContainer(GRID_SLOTS);
    private final ResultContainer result = new ResultContainer();
    private final Container blueprints;
    private final ContainerLevelAccess access;
    private final ContainerListener blueprintListener = this::slotsChanged;
    @Nullable
    private GunAssemblyRecipe currentRecipe;
    /** Client display state: the screen hides the bench slots on unbuilt tabs. */
    private boolean benchSlotsVisible = true;

    /** Client-side constructor; the blueprint container mirrors the server via slot sync. */
    public GunBenchMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL, new SimpleContainer(GunBenchBlockEntity.BLUEPRINT_SLOTS));
    }

    public GunBenchMenu(int syncId, Inventory playerInventory, ContainerLevelAccess access, Container blueprints) {
        super(MilicraftMenus.GUN_BENCH.get(), syncId);
        this.access = access;
        this.blueprints = blueprints;
        this.parts.addListener(this::slotsChanged);
        if (blueprints instanceof SimpleContainer simple) {
            simple.addListener(this.blueprintListener);
        }

        for (int i = 0; i < GRID_SLOTS; i++) {
            this.addSlot(new PartSlot(parts, i,
                    GRID_X + (i % GRID_COLS) * GRID_PITCH, GRID_Y + (i / GRID_COLS) * GRID_PITCH));
        }
        this.addSlot(new AssemblyResultSlot(result, 0, RESULT_X, RESULT_Y));
        for (int i = 0; i < GunBenchBlockEntity.BLUEPRINT_SLOTS; i++) {
            this.addSlot(new BlueprintSlot(blueprints, i,
                    BLUEPRINT_X + (i % 2) * 18, BLUEPRINT_Y + (i / 2) * 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 58 + col * 18, 166 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 58 + col * 18, 226));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, MilicraftBlocks.GUN_BENCH.get());
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        createResult();
    }

    private void createResult() {
        Optional<GunAssemblyRecipe> match = GunAssemblyRecipes.matchShaped(gridStacks());
        this.currentRecipe = match.orElse(null);
        boolean craftable = match.isPresent() && hasBlueprintFor(match.get());
        this.result.setItem(0, craftable ? GunAssemblyRecipes.assemble(match.get()) : ItemStack.EMPTY);
        broadcastChanges();
    }

    private List<ItemStack> gridStacks() {
        List<ItemStack> grid = new ArrayList<>(GRID_SLOTS);
        for (int i = 0; i < GRID_SLOTS; i++) {
            grid.add(parts.getItem(i));
        }
        return grid;
    }

    /** Whether a blueprint for the recipe's gun sits in the bench sidebar. */
    public boolean hasBlueprintFor(GunAssemblyRecipe recipe) {
        for (int i = 0; i < UNLOCKED_BLUEPRINT_SLOTS; i++) {
            if (BlueprintItem.gunFor(blueprints.getItem(i)) == recipe.result().get()) {
                return true;
            }
        }
        return false;
    }

    private Map<GunPartType, Integer> partCounts() {
        Map<GunPartType, Integer> counts = new EnumMap<>(GunPartType.class);
        for (int i = 0; i < GRID_SLOTS; i++) {
            if (parts.getItem(i).getItem() instanceof GunPartItem part) {
                counts.merge(part.getPartType(), parts.getItem(i).getCount(), Integer::sum);
            }
        }
        return counts;
    }

    private void consumeParts() {
        GunAssemblyRecipe recipe = this.currentRecipe;
        if (recipe == null) {
            return;
        }
        // Shaped: one part comes out of each slot the recipe's layout occupies.
        GunPartType[] layout = GunAssemblyRecipes.layoutFor(recipe);
        for (int i = 0; i < GRID_SLOTS; i++) {
            if (layout[i] != null) {
                parts.removeItem(i, 1);
            }
        }
    }

    /** The recipe the current grid contents would assemble, for the screen's preview/stats. */
    @Nullable
    public GunAssemblyRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    /** Parts currently placed of the given type, for the screen's ghost overlays. */
    public int countPlaced(GunPartType type) {
        return partCounts().getOrDefault(type, 0);
    }

    /**
     * Shows/hides the bench slots (items stay in them; they just stop rendering and
     * responding to hover). Used by the screen when switching to an unbuilt tab.
     */
    public void setBenchSlotsVisible(boolean visible) {
        this.benchSlotsVisible = visible;
    }

    /**
     * First player-inventory slot (menu index) holding a part of the given type, or -1.
     * The screen uses the same lookup to animate the fill from the right slot.
     */
    public int findPartSourceSlot(GunPartType type) {
        for (int i = INV_START; i < this.slots.size(); i++) {
            ItemStack stack = this.slots.get(i).getItem();
            if (stack.getItem() instanceof GunPartItem part && part.getPartType() == type) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Grid slot (menu index) a part of the given type would land in: its next empty
     * gun-shape slot, so filled parts land exactly where the ghosts show them;
     * -1 when all its shape slots are taken.
     */
    public int findGridDestSlot(GunPartType type) {
        for (int i : GunAssemblyRecipes.preferredSlots(type)) {
            if (parts.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /** Packs a catalog fill request into a menu button id: low 3 bits type, rest count. */
    public static int fillButtonId(GunPartType type, int count) {
        return type.ordinal() | (count << 3);
    }

    /**
     * Catalog click-to-fill: moves up to the requested number of parts of the given
     * type from the player's inventory into the grid, one per gun-shape slot.
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        int typeOrdinal = id & 7;
        int count = Math.min(id >> 3, GRID_SLOTS);
        if (typeOrdinal >= GunPartType.values().length || count <= 0) {
            return false;
        }
        GunPartType type = GunPartType.values()[typeOrdinal];
        boolean moved = false;
        for (int i = 0; i < count; i++) {
            int source = findPartSourceSlot(type);
            int dest = findGridDestSlot(type);
            if (source < 0 || dest < 0) {
                break;
            }
            ItemStack sourceStack = this.slots.get(source).getItem();
            parts.setItem(dest, sourceStack.copyWithCount(1));
            sourceStack.shrink(1);
            this.slots.get(source).setChanged();
            moved = true;
        }
        return moved;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.parts));
        if (this.blueprints instanceof SimpleContainer simple) {
            simple.removeListener(this.blueprintListener);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index == RESULT_SLOT) {
                // result -> inventory. Returning the copy (with onTake refilling the
                // slot from remaining parts) lets vanilla's quick-move loop craft-all.
                if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onTake(player, stack);
                return copied;
            } else if (index < INV_START) {
                // grid or blueprint slot -> inventory
                if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof BlueprintItem) {
                if (!this.moveItemStackTo(stack, BLUEPRINT_START, BLUEPRINT_START + UNLOCKED_BLUEPRINT_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof GunPartItem part) {
                // inventory -> grid: one unit into the part's next gun-shape slot,
                // so shift-clicking distributes into the shape (vanilla's quick-move
                // loop repeats this while the stack lasts and slots remain).
                int dest = findGridDestSlot(part.getPartType());
                if (dest < 0) {
                    return ItemStack.EMPTY;
                }
                parts.setItem(dest, stack.copyWithCount(1));
                stack.shrink(1);
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copied;
    }

    private class PartSlot extends Slot {
        PartSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof GunPartItem;
        }

        @Override
        public boolean isActive() {
            return benchSlotsVisible;
        }
    }

    private class BlueprintSlot extends Slot {
        BlueprintSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        private boolean unlocked() {
            return getContainerSlot() < UNLOCKED_BLUEPRINT_SLOTS;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return unlocked() && stack.getItem() instanceof BlueprintItem;
        }

        @Override
        public boolean mayPickup(Player player) {
            return unlocked();
        }

        @Override
        public boolean isActive() {
            return benchSlotsVisible;
        }
    }

    private class AssemblyResultSlot extends Slot {
        AssemblyResultSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            consumeParts();
            super.onTake(player, stack);
        }

        @Override
        public boolean isActive() {
            return benchSlotsVisible;
        }
    }
}
