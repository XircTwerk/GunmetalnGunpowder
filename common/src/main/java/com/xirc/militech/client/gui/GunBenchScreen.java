package com.xirc.militech.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xirc.militech.common.data.gun.GunAssemblyRecipe;
import com.xirc.militech.common.data.gun.GunAssemblyRecipes;
import com.xirc.militech.common.data.gun.GunStats;
import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.common.item.GunPartType;
import com.xirc.militech.common.menu.GunBenchMenu;
import com.xirc.militech.registry.MilitechItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Screen for {@link GunBenchMenu}. Blueprint storage hangs off the left edge, a
 * vertical parts catalog sits inside the left panel, the 4x3 part grid fills the
 * middle (ghosts laid out in a gun shape), and a rotating 3D preview with a live
 * stat block sits on the right. Gun categories are square icon tabs hanging off
 * the right edge. Background is drawn programmatically like {@link AmmoBoxScreen}.
 */
public class GunBenchScreen extends AbstractContainerScreen<GunBenchMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int EDGE_DARK = 0xFF555555;
    private static final int HIGHLIGHT = 0xFFFFFFFF;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SLOT_SHADOW = 0xFF373737;
    private static final int GHOST_OVERLAY = 0xAAC6C6C6;
    private static final int MISSING_TINT = 0x66FF0000;
    private static final int LOCKED_OVERLAY = 0xAA333333;
    private static final int PREVIEW_BG = 0xFF2B2B2B;
    private static final int BUTTON_FACE = 0xFFA0A0A0;
    private static final int BUTTON_FACE_SELECTED = 0xFF707070;

    // Parts catalog: vertical column under the "Parts" label, centered on the left side.
    private static final int CATALOG_X = 12;
    private static final int CATALOG_Y = 58;
    private static final int CATALOG_LABEL_Y = 46;
    private static final int CATALOG_STEP = 20;

    // Blueprint sidebar panel, hanging off the left edge (2 columns x 4 rows of slots).
    private static final int BLUEPRINT_PANEL_X1 = -50;
    private static final int BLUEPRINT_PANEL_Y1 = 14;
    private static final int BLUEPRINT_PANEL_X2 = 0;
    private static final int BLUEPRINT_PANEL_Y2 = 104;

    // Category tab strip: square icon tabs hanging off the right edge.
    private static final int TAB_SIZE = 24;
    private static final int TAB_STEP = 26;
    private static final int TAB_Y = 14;
    private static final int TABS_VISIBLE = 6;

    // Preview zone
    private static final int PREVIEW_X1 = 174;
    private static final int PREVIEW_Y1 = 14;
    private static final int PREVIEW_X2 = 268;
    private static final int PREVIEW_Y2 = 88;
    private static final float PREVIEW_SCALE = 20.0f;

    private static final int STATS_X = 176;
    private static final int STATS_Y = 108;
    private static final int STATS_STEP = 9;

    private static final GunPartType[] PART_TYPES = GunPartType.values();

    /** Bench work modes; only ASSEMBLY is implemented, the rest show a coming-soon panel. */
    private enum BenchTab {
        ASSEMBLY("assembly", true),
        ATTACHMENTS("attachments", false),
        WRAPPING("wrapping", false),
        REPAIRMENT("repairment", false);

        final String id;
        final boolean implemented;

        BenchTab(String id, boolean implemented) {
            this.id = id;
            this.implemented = implemented;
        }

        Component title() {
            return Component.translatable("gui.militech.gun_bench.tab." + id);
        }
    }

    /** A part flying in a line from its inventory slot to its grid slot. */
    private record FlyingPart(ItemStack stack, int fromX, int fromY, int toX, int toY, long start) {
    }

    private static final long FLY_DURATION_MS = 250;

    private BenchTab currentTab = BenchTab.ASSEMBLY;
    private Button prevGunButton;
    private Button nextGunButton;
    private final List<FlyingPart> flyingParts = new ArrayList<>();

    // Preview rotation: spins on its own until dragged; dragging rotates yaw and
    // pitch, and the displayed angles ease toward the targets for smooth motion.
    private float previewYaw = -45.0f;
    private float previewYawDisplayed = -45.0f;
    private float previewPitch = -15.0f;
    private float previewPitchDisplayed = -15.0f;
    private float previewYawVelocity;
    private float previewPitchVelocity;
    private boolean previewDragging;
    private long previewLastFrame = Util.getMillis();

    /** null = all categories. */
    private String selectedCategory;
    private int categoryScroll;
    private int targetIndex = 0;
    private int highlightSlot = -1;
    private long highlightUntil;
    private final float[] displayedStats = new float[6];
    private boolean statsInitialized;

    public GunBenchScreen(GunBenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
        this.imageHeight = 250;
        this.inventoryLabelX = 58;
        this.inventoryLabelY = 154;
    }

    @Override
    protected void init() {
        super.init();
        this.prevGunButton = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleTarget(-1))
                .bounds(this.leftPos + PREVIEW_X1, this.topPos + 92, 12, 12).build());
        this.nextGunButton = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleTarget(1))
                .bounds(this.leftPos + PREVIEW_X2 - 12, this.topPos + 92, 12, 12).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.militech.gun_bench.switch_tab"), b -> switchTab())
                .bounds(this.leftPos + this.imageWidth / 2 - 34, this.topPos + 3, 68, 12).build());
        updateTabWidgets();
    }

    private void switchTab() {
        BenchTab[] tabs = BenchTab.values();
        this.currentTab = tabs[(this.currentTab.ordinal() + 1) % tabs.length];
        updateTabWidgets();
    }

    private void updateTabWidgets() {
        boolean assembly = this.currentTab.implemented;
        this.prevGunButton.visible = assembly;
        this.nextGunButton.visible = assembly;
        // Hide the bench slots entirely on unbuilt tabs; their contents stay put.
        this.menu.setBenchSlotsVisible(assembly);
    }

    // ------------------------------------------------------------------ categories

    /** All category tab entries: null (all guns) followed by each recipe category. */
    private List<String> categoryEntries() {
        List<String> categories = new ArrayList<>();
        categories.add(null);
        categories.addAll(GunAssemblyRecipes.categories());
        return categories;
    }

    private Component categoryName(String category) {
        return category == null
                ? Component.translatable("gun_category.militech.all")
                : Component.translatable("gun_category.militech." + category);
    }

    private ItemStack categoryIcon(String category) {
        if (category == null) {
            return new ItemStack(MilitechItems.GUN_BENCH.get());
        }
        List<GunAssemblyRecipe> recipes = GunAssemblyRecipes.inCategory(category);
        return recipes.isEmpty() ? ItemStack.EMPTY : new ItemStack(recipes.get(0).result().get());
    }

    private void renderCategoryTabs(GuiGraphics graphics, int x, int y) {
        List<String> categories = categoryEntries();
        int shown = Math.min(TABS_VISIBLE, categories.size() - categoryScroll);
        for (int i = 0; i < shown; i++) {
            String category = categories.get(categoryScroll + i);
            boolean selected = category == null ? this.selectedCategory == null : category.equals(this.selectedCategory);
            int tx = x + this.imageWidth + 2;
            int ty = y + TAB_Y + i * TAB_STEP;
            // Vanilla-button-style square: raised bevel normally, pressed when selected.
            graphics.fill(tx - 1, ty - 1, tx + TAB_SIZE + 1, ty + TAB_SIZE + 1, 0xFF000000);
            graphics.fill(tx, ty, tx + TAB_SIZE, ty + TAB_SIZE, selected ? BUTTON_FACE_SELECTED : BUTTON_FACE);
            int light = selected ? SLOT_SHADOW : HIGHLIGHT;
            int dark = selected ? HIGHLIGHT : SLOT_SHADOW;
            graphics.fill(tx, ty, tx + TAB_SIZE, ty + 1, light);
            graphics.fill(tx, ty, tx + 1, ty + TAB_SIZE, light);
            graphics.fill(tx, ty + TAB_SIZE - 1, tx + TAB_SIZE, ty + TAB_SIZE, dark);
            graphics.fill(tx + TAB_SIZE - 1, ty, tx + TAB_SIZE, ty + TAB_SIZE, dark);
            graphics.renderFakeItem(categoryIcon(category), tx + 4, ty + 4);
        }
        if (categories.size() > TABS_VISIBLE) {
            int tx = x + this.imageWidth + 2;
            if (categoryScroll > 0) {
                graphics.drawString(this.font, "^", tx + 7, y + TAB_Y - 10, 0xFF404040, false);
            }
            if (categoryScroll + TABS_VISIBLE < categories.size()) {
                graphics.drawString(this.font, "v", tx + 7, y + TAB_Y + TABS_VISIBLE * TAB_STEP + 2, 0xFF404040, false);
            }
        }
    }

    private int categoryTabAt(double mouseX, double mouseY) {
        List<String> categories = categoryEntries();
        int shown = Math.min(TABS_VISIBLE, categories.size() - categoryScroll);
        for (int i = 0; i < shown; i++) {
            if (isHovering(this.imageWidth + 2, TAB_Y + i * TAB_STEP, TAB_SIZE, TAB_SIZE, mouseX, mouseY)) {
                return categoryScroll + i;
            }
        }
        return -1;
    }

    private boolean overCategoryStrip(double mouseX, double mouseY) {
        return isHovering(this.imageWidth + 2, TAB_Y, TAB_SIZE, TABS_VISIBLE * TAB_STEP, mouseX, mouseY);
    }

    private List<GunAssemblyRecipe> visibleRecipes() {
        return GunAssemblyRecipes.inCategory(this.selectedCategory);
    }

    private void cycleTarget(int direction) {
        int size = visibleRecipes().size();
        this.targetIndex = Math.floorMod(this.targetIndex + direction, size);
    }

    private GunAssemblyRecipe targetRecipe() {
        List<GunAssemblyRecipe> recipes = visibleRecipes();
        if (this.targetIndex >= recipes.size()) {
            this.targetIndex = 0;
        }
        return recipes.get(this.targetIndex);
    }

    /** Gun shown in the preview and stat block: the actual result if one is assembled, otherwise the target. */
    private ItemStack previewStack() {
        ItemStack result = this.menu.slots.get(GunBenchMenu.RESULT_SLOT).getItem();
        if (!result.isEmpty()) {
            return result;
        }
        return new ItemStack(targetRecipe().result().get());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Active bench tab name, centered under the switch button.
        Component tabTitle = this.currentTab.title();
        graphics.drawString(this.font, tabTitle,
                this.leftPos + this.imageWidth / 2 - this.font.width(tabTitle) / 2,
                this.topPos + 18, 0xFF404040, false);

        if (!this.currentTab.implemented) {
            renderComingSoon(graphics);
            if (this.hoveredSlot == null || this.hoveredSlot.index >= GunBenchMenu.INV_START) {
                this.renderTooltip(graphics, mouseX, mouseY);
            }
            return;
        }

        renderFlyingParts(graphics);
        renderCatalogTooltip(graphics, mouseX, mouseY);
        int tab = categoryTabAt(mouseX, mouseY);
        if (tab >= 0) {
            graphics.renderTooltip(this.font, categoryName(categoryEntries().get(tab)), mouseX, mouseY);
        }
        renderBlueprintTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /** Covers the bench area (slots included) on tabs that aren't built yet. */
    private void renderComingSoon(GuiGraphics graphics) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 200);
        graphics.fill(this.leftPos + 3, this.topPos + 28, this.leftPos + this.imageWidth - 3, this.topPos + 160, PANEL);
        Component text = Component.translatable("gui.militech.gun_bench.coming_soon", this.currentTab.title());
        graphics.drawString(this.font, text,
                this.leftPos + this.imageWidth / 2 - this.font.width(text) / 2,
                this.topPos + 88, 0xFF7A7A7A, false);
        pose.popPose();
    }

    private void renderFlyingParts(GuiGraphics graphics) {
        long now = Util.getMillis();
        Iterator<FlyingPart> iterator = this.flyingParts.iterator();
        while (iterator.hasNext()) {
            FlyingPart fly = iterator.next();
            float progress = (now - fly.start()) / (float) FLY_DURATION_MS;
            if (progress < 0.0f) {
                continue;
            }
            if (progress >= 1.0f) {
                iterator.remove();
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_IRON, 1.0f));
                continue;
            }
            int fx = this.leftPos + Math.round(Mth.lerp(progress, fly.fromX(), fly.toX()));
            int fy = this.topPos + Math.round(Mth.lerp(progress, fly.fromY(), fly.toY()));
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(0, 0, 300);
            graphics.renderFakeItem(fly.stack(), fx, fy);
            pose.popPose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Panel base, matching AmmoBoxScreen.
        graphics.fill(x, y, x + imageWidth, y + imageHeight, EDGE_DARK);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, PANEL);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, HIGHLIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + imageHeight - 1, HIGHLIGHT);

        renderPlayerInventorySlots(graphics, x, y);
        if (this.currentTab.implemented) {
            renderBlueprintSidebar(graphics, x, y);
            renderCategoryTabs(graphics, x, y);
            renderCatalog(graphics, x, y);
            renderGrid(graphics, x, y);
            renderResultSlot(graphics, x, y);
            renderPreview(graphics, x, y, partialTick);
            renderStats(graphics, x, y);
        }
    }

    // ------------------------------------------------------------------ blueprints

    private void renderBlueprintSidebar(GuiGraphics graphics, int x, int y) {
        // Hanging side panel behind the blueprint slots.
        int x1 = x + BLUEPRINT_PANEL_X1;
        int y1 = y + BLUEPRINT_PANEL_Y1;
        int x2 = x + BLUEPRINT_PANEL_X2;
        int y2 = y + BLUEPRINT_PANEL_Y2;
        graphics.fill(x1 - 1, y1 - 1, x2, y2 + 1, EDGE_DARK);
        graphics.fill(x1, y1, x2, y2, PANEL);

        // "Blueprints" label above the boxes, scaled to fit the narrow panel.
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x1 + 2, y1 + 3, 0);
        pose.scale(0.8f, 0.8f, 1.0f);
        graphics.drawString(this.font, Component.translatable("gui.militech.gun_bench.blueprints"),
                0, 0, 0xFF404040, false);
        pose.popPose();

        for (int i = 0; i < GunBenchMenu.INV_START - GunBenchMenu.BLUEPRINT_START; i++) {
            Slot slot = this.menu.slots.get(GunBenchMenu.BLUEPRINT_START + i);
            drawSlot(graphics, x + slot.x, y + slot.y);
            if (i >= GunBenchMenu.UNLOCKED_BLUEPRINT_SLOTS) {
                graphics.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, LOCKED_OVERLAY);
            }
        }
    }

    private void renderBlueprintTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = GunBenchMenu.UNLOCKED_BLUEPRINT_SLOTS; i < GunBenchMenu.INV_START - GunBenchMenu.BLUEPRINT_START; i++) {
            Slot slot = this.menu.slots.get(GunBenchMenu.BLUEPRINT_START + i);
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                graphics.renderTooltip(this.font,
                        Component.translatable("gui.militech.gun_bench.slot_locked"), mouseX, mouseY);
                return;
            }
        }
    }

    private boolean overBlueprintPanel(double mouseX, double mouseY) {
        return isHovering(BLUEPRINT_PANEL_X1, BLUEPRINT_PANEL_Y1,
                BLUEPRINT_PANEL_X2 - BLUEPRINT_PANEL_X1, BLUEPRINT_PANEL_Y2 - BLUEPRINT_PANEL_Y1, mouseX, mouseY);
    }

    // ------------------------------------------------------------------ part grid

    private void renderGrid(GuiGraphics graphics, int x, int y) {
        for (int i = 0; i < GunBenchMenu.GRID_SLOTS; i++) {
            Slot slot = this.menu.slots.get(i);
            drawSlot(graphics, x + slot.x, y + slot.y);
        }

        renderGridGhosts(graphics, x, y);

        // Pulsing glow on the slot a catalog click just filled.
        if (this.highlightSlot >= 0 && System.currentTimeMillis() < this.highlightUntil) {
            Slot slot = this.menu.slots.get(this.highlightSlot);
            float pulse = (Mth.sin(System.currentTimeMillis() / 120.0f) + 1.0f) * 0.5f;
            int alpha = (int) (100 + pulse * 155);
            int glow = (alpha << 24) | 0xFFD700;
            drawHollowRect(graphics, x + slot.x - 2, y + slot.y - 2, x + slot.x + 18, y + slot.y + 18, glow);
        }
    }

    /**
     * Ghosts the target recipe's shaped layout into the grid - one ghost in every
     * layout slot that is still empty; red when the player doesn't own enough of
     * that part. The same layout drives matching and click-to-fill, so the shape
     * never shifts as slots fill up.
     */
    private void renderGridGhosts(GuiGraphics graphics, int x, int y) {
        GunAssemblyRecipe target = targetRecipe();
        GunPartType[] layout = GunAssemblyRecipes.layoutFor(target);
        for (int i = 0; i < GunBenchMenu.GRID_SLOTS; i++) {
            GunPartType type = layout[i];
            if (type == null || this.menu.slots.get(i).hasItem()) {
                continue;
            }
            Slot slot = this.menu.slots.get(i);
            boolean missing = countOwned(type) < target.required(type);
            renderGhostPart(graphics, x + slot.x, y + slot.y, type, missing);
        }
    }

    private void renderGhostPart(GuiGraphics graphics, int sx, int sy, GunPartType type, boolean missing) {
        graphics.renderFakeItem(new ItemStack(MilitechItems.partFor(type)), sx, sy);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 250);
        graphics.fill(sx, sy, sx + 16, sy + 16, missing ? MISSING_TINT : GHOST_OVERLAY);
        pose.popPose();
    }

    private void renderResultSlot(GuiGraphics graphics, int x, int y) {
        Slot slot = this.menu.slots.get(GunBenchMenu.RESULT_SLOT);
        graphics.drawString(this.font, ">", x + slot.x - 8, y + slot.y + 4, 0xFF404040, false);
        drawSlot(graphics, x + slot.x, y + slot.y);
    }

    private void renderPlayerInventorySlots(GuiGraphics graphics, int x, int y) {
        for (int i = GunBenchMenu.INV_START; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            drawSlot(graphics, x + slot.x, y + slot.y);
        }
    }

    // ------------------------------------------------------------------ catalog

    private void renderCatalog(GuiGraphics graphics, int x, int y) {
        graphics.drawString(this.font, Component.translatable("gui.militech.gun_bench.parts"),
                x + 8, y + CATALOG_LABEL_Y, 0xFF404040, false);
        for (int i = 0; i < PART_TYPES.length; i++) {
            int ex = x + CATALOG_X;
            int ey = y + CATALOG_Y + i * CATALOG_STEP;
            drawSlot(graphics, ex, ey);
            ItemStack stack = new ItemStack(MilitechItems.partFor(PART_TYPES[i]));
            graphics.renderFakeItem(stack, ex, ey);
            int owned = countOwned(PART_TYPES[i]);
            if (owned > 0) {
                PoseStack pose = graphics.pose();
                pose.pushPose();
                pose.translate(0, 0, 260);
                String label = String.valueOf(owned);
                graphics.drawString(this.font, label, ex + 17 - this.font.width(label), ey + 9, 0xFFFFFF, true);
                pose.popPose();
            }
        }
    }

    private void renderCatalogTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = catalogIndexAt(mouseX, mouseY);
        if (index < 0) {
            return;
        }
        GunPartType type = PART_TYPES[index];
        Item item = MilitechItems.partFor(type);
        List<Component> lines = new ArrayList<>();
        lines.add(item.getDescription());
        if (countOwned(type) <= 0) {
            lines.add(Component.translatable("gui.militech.gun_bench.not_owned").withStyle(ChatFormatting.RED));
            lines.add(Component.translatable("tooltip.militech.part." + type.id() + ".source").withStyle(ChatFormatting.GRAY));
        }
        graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    private int catalogIndexAt(double mouseX, double mouseY) {
        for (int i = 0; i < PART_TYPES.length; i++) {
            if (isHovering(CATALOG_X, CATALOG_Y + i * CATALOG_STEP, 16, 16, mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    private int countOwned(GunPartType type) {
        Item item = MilitechItems.partFor(type);
        Inventory inventory = this.minecraft.player.getInventory();
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        // Include parts already placed in the bench.
        return count + this.menu.countPlaced(type);
    }

    // ------------------------------------------------------------------ preview

    private void renderPreview(GuiGraphics graphics, int x, int y, float partialTick) {
        int x1 = x + PREVIEW_X1;
        int y1 = y + PREVIEW_Y1;
        int x2 = x + PREVIEW_X2;
        int y2 = y + PREVIEW_Y2;
        graphics.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, SLOT_SHADOW);
        graphics.fill(x1, y1, x2, y2, PREVIEW_BG);

        ItemStack stack = previewStack();
        boolean known = this.menu.hasBlueprintFor(targetRecipe());

        graphics.enableScissor(x1, y1, x2, y2);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate((x1 + x2) / 2.0f, (y1 + y2) / 2.0f, 150);
        pose.scale(PREVIEW_SCALE, -PREVIEW_SCALE, PREVIEW_SCALE);
        long now = Util.getMillis();
        if (!this.previewDragging) {
            // Thrown momentum decays back into the idle spin.
            this.previewYaw += (now - this.previewLastFrame) * 0.05f + this.previewYawVelocity;
            this.previewPitch += this.previewPitchVelocity;
            this.previewYawVelocity *= 0.94f;
            this.previewPitchVelocity *= 0.94f;
        }
        this.previewLastFrame = now;
        this.previewYawDisplayed = Mth.lerp(0.25f, this.previewYawDisplayed, this.previewYaw);
        this.previewPitchDisplayed = Mth.lerp(0.25f, this.previewPitchDisplayed, this.previewPitch);
        pose.mulPose(Axis.XP.rotationDegrees(this.previewPitchDisplayed));
        pose.mulPose(Axis.YP.rotationDegrees(this.previewYawDisplayed));
        Lighting.setupForEntityInInventory();
        if (!known) {
            RenderSystem.setShaderColor(0.05f, 0.05f, 0.05f, 1.0f);
        }
        this.minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose,
                graphics.bufferSource(), this.minecraft.level, 0);
        graphics.flush();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Lighting.setupFor3DItems();
        pose.popPose();

        // Blueprint gate notice, drawn small inside the preview box.
        if (!known) {
            Component locked = Component.translatable("gui.militech.gun_bench.unknown_recipe");
            pose.pushPose();
            pose.translate(x + (PREVIEW_X1 + PREVIEW_X2) / 2.0f - this.font.width(locked) * 0.25f, y2 - 8, 0);
            pose.scale(0.5f, 0.5f, 1.0f);
            graphics.drawString(this.font, locked, 0, 0, 0xFFFF5555, true);
            pose.popPose();
        }
        graphics.disableScissor();

        // Target gun name centered between the cycle buttons; hidden without a blueprint.
        Component name = known ? stack.getHoverName() : Component.literal("???");
        int nameX = x + (PREVIEW_X1 + PREVIEW_X2) / 2 - this.font.width(name) / 2;
        graphics.drawString(this.font, name, nameX, y + 94, 0xFF404040, false);
    }

    // ------------------------------------------------------------------ stats

    private record StatRow(String key, boolean lowerIsBetter, boolean integer) {
    }

    private static final StatRow[] STAT_ROWS = {
            new StatRow("damage", false, false),
            new StatRow("fire_rate", false, true),
            new StatRow("reload", true, false),
            new StatRow("spread", true, false),
            new StatRow("magazine", false, true),
            new StatRow("range", false, true),
    };

    private static float[] statValues(GunStats stats) {
        float rpm = stats.refireCooldownTicks() <= 0 ? 1200.0f : 1200.0f / stats.refireCooldownTicks();
        return new float[]{
                stats.damage(),
                rpm,
                stats.reloadDurationTicks() / 20.0f,
                stats.spread() * 100.0f,
                stats.maxRounds(),
                stats.range(),
        };
    }

    private void renderStats(GuiGraphics graphics, int x, int y) {
        if (!(previewStack().getItem() instanceof AbstractGunItem gun)) {
            return;
        }
        if (!this.menu.hasBlueprintFor(targetRecipe())) {
            for (int i = 0; i < STAT_ROWS.length; i++) {
                int rowY = y + STATS_Y + i * STATS_STEP;
                graphics.drawString(this.font, Component.translatable("gui.militech.gun_bench.stat." + STAT_ROWS[i].key()),
                        x + STATS_X, rowY, 0xFF404040, false);
                graphics.drawString(this.font, "???",
                        x + PREVIEW_X2 - this.font.width("???"), rowY, 0xFF2A2A2A, false);
            }
            return;
        }
        float[] targets = statValues(gun.getStats());
        if (!statsInitialized) {
            System.arraycopy(targets, 0, displayedStats, 0, targets.length);
            statsInitialized = true;
        }

        float[] heldValues = null;
        if (hasShiftDown() && this.minecraft.player.getMainHandItem().getItem() instanceof AbstractGunItem held) {
            heldValues = statValues(held.getStats());
        }

        for (int i = 0; i < STAT_ROWS.length; i++) {
            // Animated tween toward the current value instead of snapping.
            displayedStats[i] = Mth.lerp(0.25f, displayedStats[i], targets[i]);

            StatRow row = STAT_ROWS[i];
            int rowY = y + STATS_Y + i * STATS_STEP;
            graphics.drawString(this.font, Component.translatable("gui.militech.gun_bench.stat." + row.key()),
                    x + STATS_X, rowY, 0xFF404040, false);

            String value = row.integer() ? String.valueOf(Math.round(displayedStats[i]))
                    : String.format("%.1f", displayedStats[i]);
            StringBuilder text = new StringBuilder(value);
            int color = 0xFF2A2A2A;
            if (heldValues != null) {
                float delta = targets[i] - heldValues[i];
                if (Math.abs(delta) > 0.05f) {
                    boolean better = row.lowerIsBetter() ? delta < 0 : delta > 0;
                    color = better ? 0xFF15803D : 0xFFB91C1C;
                    text.append(String.format(" (%+.1f)", delta));
                }
            }
            graphics.drawString(this.font, text.toString(),
                    x + PREVIEW_X2 - this.font.width(text.toString()), rowY, color, false);
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.currentTab.implemented) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int tab = categoryTabAt(mouseX, mouseY);
        if (tab >= 0) {
            this.selectedCategory = categoryEntries().get(tab);
            this.targetIndex = 0;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            return true;
        }
        int index = catalogIndexAt(mouseX, mouseY);
        if (index >= 0) {
            clickCatalogEntry(PART_TYPES[index]);
            return true;
        }
        if (button == 0 && isHovering(PREVIEW_X1, PREVIEW_Y1, PREVIEW_X2 - PREVIEW_X1, PREVIEW_Y2 - PREVIEW_Y1, mouseX, mouseY)) {
            this.previewDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.previewDragging && button == 0) {
            this.previewYaw += (float) dragX * 1.5f;
            this.previewPitch += (float) dragY * 1.5f;
            this.previewYawVelocity = (float) dragX * 1.5f;
            this.previewPitchVelocity = (float) dragY * 1.5f;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.previewDragging && button == 0) {
            this.previewDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<String> categories = categoryEntries();
        if (this.currentTab.implemented && categories.size() > TABS_VISIBLE && overCategoryStrip(mouseX, mouseY)) {
            this.categoryScroll = Mth.clamp(this.categoryScroll - (int) Math.signum(delta), 0, categories.size() - TABS_VISIBLE);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        // The blueprint panel and category strip hang outside the main panel; clicks
        // there must not count as "outside" (which would drop the carried item).
        if (overBlueprintPanel(mouseX, mouseY) || overCategoryStrip(mouseX, mouseY)) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    /**
     * Catalog click-to-fill: asks the server to move every part of this type the
     * target recipe still needs from the inventory into the grid, animating each
     * one flying into its shape slot. Does nothing when the shape needs no more.
     */
    private void clickCatalogEntry(GunPartType type) {
        GunPartType[] layout = GunAssemblyRecipes.layoutFor(targetRecipe());
        List<Integer> destSlots = new ArrayList<>();
        for (int i = 0; i < GunBenchMenu.GRID_SLOTS; i++) {
            if (layout[i] == type && !this.menu.slots.get(i).hasItem()) {
                destSlots.add(i);
            }
        }
        int source = this.menu.findPartSourceSlot(type);
        int inInventory = countOwned(type) - this.menu.countPlaced(type);
        int moves = Math.min(destSlots.size(), inInventory);
        if (moves <= 0 || source < 0) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId,
                GunBenchMenu.fillButtonId(type, moves));
        Slot from = this.menu.slots.get(source);
        long now = Util.getMillis();
        for (int i = 0; i < moves; i++) {
            Slot to = this.menu.slots.get(destSlots.get(i));
            this.flyingParts.add(new FlyingPart(new ItemStack(MilitechItems.partFor(type)),
                    from.x, from.y, to.x, to.y, now + i * 70L));
        }
        this.highlightSlot = destSlots.get(moves - 1);
        this.highlightUntil = System.currentTimeMillis() + 2000;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (!this.currentTab.implemented) {
            // Bench slots are hidden on unbuilt tabs; also block quick-moves that
            // would land items into them.
            if (slotId >= 0 && slotId < GunBenchMenu.INV_START) {
                return;
            }
            if (type == ClickType.QUICK_MOVE) {
                return;
            }
        }
        boolean placingPart = slot != null && slotId >= 0 && slotId < GunBenchMenu.GRID_SLOTS
                && !this.menu.getCarried().isEmpty()
                && slot.mayPlace(this.menu.getCarried());
        super.slotClicked(slot, slotId, mouseButton, type);
        if (placingPart) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_IRON, 1.0f));
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void drawSlot(GuiGraphics graphics, int sx, int sy) {
        graphics.fill(sx - 1, sy - 1, sx + 17, sy, SLOT_SHADOW);
        graphics.fill(sx - 1, sy - 1, sx, sy + 17, SLOT_SHADOW);
        graphics.fill(sx - 1, sy + 16, sx + 17, sy + 17, HIGHLIGHT);
        graphics.fill(sx + 16, sy - 1, sx + 17, sy + 17, HIGHLIGHT);
        graphics.fill(sx, sy, sx + 16, sy + 16, SLOT_FILL);
    }

    private static void drawHollowRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y1 + 1, color);
        graphics.fill(x1, y2 - 1, x2, y2, color);
        graphics.fill(x1, y1, x1 + 1, y2, color);
        graphics.fill(x2 - 1, y1, x2, y2, color);
    }
}
