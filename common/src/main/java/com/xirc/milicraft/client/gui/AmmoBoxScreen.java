package com.xirc.milicraft.client.gui;

import com.xirc.milicraft.common.menu.AmmoBoxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Screen for {@link AmmoBoxMenu}. The background is drawn programmatically to match the
 * vanilla inventory look (grey panel, beveled border, inset slots) so the box GUI needs
 * no texture asset while still reading as a normal Minecraft container.
 */
public class AmmoBoxScreen extends AbstractContainerScreen<AmmoBoxMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int EDGE_DARK = 0xFF555555;
    private static final int HIGHLIGHT = 0xFFFFFFFF;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SLOT_SHADOW = 0xFF373737;

    public AmmoBoxScreen(AmmoBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // Panel: dark outer edge, grey field, light top/left highlight.
        graphics.fill(x, y, x + w, y + h, EDGE_DARK);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, HIGHLIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, HIGHLIGHT);

        // Inset slots, matching vanilla (dark top-left, light bottom-right).
        for (Slot slot : this.menu.slots) {
            drawSlot(graphics, x + slot.x, y + slot.y);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int sx, int sy) {
        graphics.fill(sx - 1, sy - 1, sx + 17, sy, SLOT_SHADOW);
        graphics.fill(sx - 1, sy - 1, sx, sy + 17, SLOT_SHADOW);
        graphics.fill(sx - 1, sy + 16, sx + 17, sy + 17, HIGHLIGHT);
        graphics.fill(sx + 16, sy - 1, sx + 17, sy + 17, HIGHLIGHT);
        graphics.fill(sx, sy, sx + 16, sy + 16, SLOT_FILL);
    }
}
