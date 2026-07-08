package com.xirc.milicraft.common.data.gun;

import com.xirc.milicraft.common.item.AbstractGunItem;
import com.xirc.milicraft.common.item.GunPartItem;
import com.xirc.milicraft.common.item.GunPartType;
import com.xirc.milicraft.registry.MilicraftGuns;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default gun bench recipes, hardcoded like {@link GunStatsDefaults}. Each gun is a
 * defined set of part counts; the bench matches the placed parts against the active
 * recipe list ({@link GunAssemblyManager} datapack recipes when present, else these).
 */
public final class GunAssemblyRecipes {
    public static final List<GunAssemblyRecipe> RECIPES = List.of(
            new GunAssemblyRecipe(MilicraftGuns.BERETTA, "pistol", Map.of(
                    GunPartType.FRAME, 1,
                    GunPartType.BARREL, 1,
                    GunPartType.MECHANISM, 1,
                    GunPartType.COMPONENT, 1,
                    GunPartType.MAGAZINE, 1)),
            new GunAssemblyRecipe(MilicraftGuns.ASSAULT_RIFLE, "rifle", Map.of(
                    GunPartType.FRAME, 1,
                    GunPartType.BARREL, 2,
                    GunPartType.MECHANISM, 1,
                    GunPartType.COMPONENT, 2,
                    GunPartType.MAGAZINE, 1)));

    /** The active recipe list: datapack recipes when loaded, otherwise the defaults. */
    public static List<GunAssemblyRecipe> all() {
        return GunAssemblyManager.recipes();
    }

    /** Distinct recipe categories in declaration order, for the bench screen's tabs. */
    public static List<String> categories() {
        return all().stream().map(GunAssemblyRecipe::category).distinct().toList();
    }

    /** Recipes in the given category, or all recipes for {@code null}. */
    public static List<GunAssemblyRecipe> inCategory(String category) {
        if (category == null) {
            return all();
        }
        return all().stream().filter(recipe -> recipe.category().equals(category)).toList();
    }

    private GunAssemblyRecipes() {
    }

    /**
     * Preferred bench grid slots per part type (4 cols x 3 rows, ordinals of
     * {@link GunPartType}), arranged so a laid-out recipe forms a right-facing gun:
     * stock-frame-barrel along the top row, grip and magazine hanging below.
     */
    private static final int[][] PREFERRED_SLOTS = {
            {1, 9},          // FRAME - receiver
            {2, 3, 7, 11},   // BARREL - extending right
            {5, 9},          // MECHANISM - grip under receiver
            {0, 4, 8},       // COMPONENT - stock, left
            {6, 10},         // MAGAZINE - under the barrel root
    };
    public static final int GRID_SLOTS = 12;

    /**
     * The recipe's canonical shaped layout: which part type each grid slot must
     * hold ({@code null} = must be empty). Deterministic, so the bench ghosts,
     * click-to-fill and shaped matching all agree on the same gun shape.
     */
    public static GunPartType[] layoutFor(GunAssemblyRecipe recipe) {
        GunPartType[] layout = new GunPartType[GRID_SLOTS];
        for (GunPartType type : GunPartType.values()) {
            int units = recipe.required(type);
            for (int slot : PREFERRED_SLOTS[type.ordinal()]) {
                if (units <= 0) {
                    break;
                }
                if (layout[slot] == null) {
                    layout[slot] = type;
                    units--;
                }
            }
            for (int slot = 0; slot < GRID_SLOTS && units > 0; slot++) {
                if (layout[slot] == null) {
                    layout[slot] = type;
                    units--;
                }
            }
        }
        return layout;
    }

    /** Preferred grid slots for one part type, in ghost/fill order. */
    public static int[] preferredSlots(GunPartType type) {
        return PREFERRED_SLOTS[type.ordinal()];
    }

    /**
     * Shaped match: the grid must hold exactly each recipe's layout - the right
     * part type in each expected slot and nothing anywhere else.
     */
    public static Optional<GunAssemblyRecipe> matchShaped(List<ItemStack> grid) {
        for (GunAssemblyRecipe recipe : all()) {
            if (matchesShaped(recipe, grid)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private static boolean matchesShaped(GunAssemblyRecipe recipe, List<ItemStack> grid) {
        GunPartType[] layout = layoutFor(recipe);
        for (int i = 0; i < GRID_SLOTS; i++) {
            ItemStack stack = grid.get(i);
            if (layout[i] == null) {
                if (!stack.isEmpty()) {
                    return false;
                }
            } else if (!(stack.getItem() instanceof GunPartItem part) || part.getPartType() != layout[i]) {
                return false;
            }
        }
        return true;
    }

    /** Assembled guns come out unloaded; the player reloads them like any other gun. */
    public static ItemStack assemble(GunAssemblyRecipe recipe) {
        ItemStack stack = new ItemStack(recipe.result().get());
        stack.getOrCreateTag().putInt(AbstractGunItem.SHOTS_ID, 0);
        return stack;
    }
}
