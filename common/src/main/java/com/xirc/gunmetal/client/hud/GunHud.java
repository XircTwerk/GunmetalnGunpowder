package com.xirc.gunmetal.client.hud;

import com.xirc.gunmetal.common.item.AbstractGunItem;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GunHud {
    private static final int WIDTH = 136;
    private static final int HEIGHT = 52;
    private static final int PADDING = 5;
    private static final int AMMO_WIDTH = 78;
    private static final float STATE_SCALE = 0.75f;

    private static final int TEXT = 0xFFE8E0CF;
    private static final int MUTED = 0xFF9C9485;
    private static final int AMMO = 0xFFFFE08A;
    private static final int EMPTY = 0xFFFF6F5F;
    private static final int READY = 0xFFBFEA8A;
    private static final int RELOAD = 0xFFFFB347;
    private static final int BAR_BACK = 0xFF2B2B2B;
    private static final int BAR_BORDER = 0xFF050505;
    private static final int BAR_GOOD = 0xFF63D66E;
    private static final int BAR_WARN = 0xFFE2C65F;

    private GunHud() {
    }

    public static void init() {
        ClientGuiEvent.RENDER_HUD.register(GunHud::render);
    }

    private static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack stack = gunStack(player);
        if (!(stack.getItem() instanceof AbstractGunItem gun)) {
            return;
        }

        int x = graphics.guiWidth() - WIDTH - 6;
        int y = graphics.guiHeight() - HEIGHT - 8;

        drawPreview(graphics, stack, x + PADDING + 2, y + PADDING);
        drawDurability(graphics, stack, x + PADDING + 1, y + 39, 34, 5);
        drawAmmo(graphics, minecraft.font, player, gun, stack, x + 46, y + PADDING);
    }

    private static ItemStack gunStack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AbstractGunItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof AbstractGunItem ? offHand : ItemStack.EMPTY;
    }

    private static void drawPreview(GuiGraphics graphics, ItemStack stack, int x, int y) {
        drawOutline(graphics, stack, x + 0.2f, y + 1.2f, 1.9f);

        graphics.pose().pushPose();
        graphics.pose().translate(x + 1.0f, y + 2.0f, 0.0f);
        graphics.pose().scale(1.8f, 1.8f, 1.0f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private static void drawOutline(GuiGraphics graphics, ItemStack stack, float x, float y, float scale) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        OutlineBufferSource outline = new OutlineBufferSource(graphics.bufferSource());
        outline.setColor(255, 255, 255, 255);

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, -10.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, graphics.pose(), outline, minecraft.level, 0);
        graphics.pose().popPose();

        outline.endOutlineBatch();
        graphics.flush();
    }

    private static void drawDurability(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BAR_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BAR_BACK);

        int color = BAR_GOOD;
        int filled = width - 2;
        if (stack.isDamageableItem()) {
            float durability = 1.0f - stack.getDamageValue() / (float) stack.getMaxDamage();
            filled = Math.round((width - 2) * Mth.clamp(durability, 0.0f, 1.0f));
            color = lowColor(BAR_GOOD, durability);
        }

        if (filled > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + height - 1, color);
        }
    }

    private static void drawAmmo(GuiGraphics graphics, Font font, Player player, AbstractGunItem gun, ItemStack stack, int x, int y) {
        int loaded = gun.getShots(stack);
        int max = gun.getMaxRounds();
        int reserve = reserveAmmo(player, gun.getAmmoItem());
        String name = stack.getHoverName().getString();
        String ammoType = gun.getAmmoItem().getDescription().getString();
        String state = state(player, gun, stack, loaded);
        int stateColor = stateColor(state);
        float ammoRatio = max <= 0 ? 0.0f : Mth.clamp(loaded / (float) max, 0.0f, 1.0f);

        graphics.drawString(font, trim(font, name, AMMO_WIDTH - 18), x, y, TEXT, false);
        drawScaledString(graphics, font, state, x + AMMO_WIDTH - Math.round(font.width(state) * STATE_SCALE), y + 1, stateColor, STATE_SCALE);

        graphics.pose().pushPose();
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        String loadedText = Integer.toString(loaded);
        int ammoColor = lowColor(AMMO, ammoRatio);
        graphics.drawString(font, loadedText, x / 2, (y + 12) / 2, ammoColor, false);
        graphics.pose().popPose();

        int reserveX = x + 5 + font.width(Integer.toString(loaded)) * 2;
        graphics.drawString(font, "/" + reserve, reserveX, y + 19, MUTED, false);
        graphics.drawString(font, trim(font, ammoType, 77), x, y + 32, MUTED, false);
    }

    private static int reserveAmmo(Player player, Item ammoItem) {
        if (player.isCreative()) {
            return 999;
        }

        int count = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() == ammoItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static String state(Player player, AbstractGunItem gun, ItemStack stack, int loaded) {
        if (AbstractGunItem.isReloading(stack)) {
            return "RELOAD";
        }
        if (loaded <= 0 && !player.isCreative()) {
            return "EMPTY";
        }
        if (player.getCooldowns().isOnCooldown(gun)) {
            return "CHAMBER";
        }
        return "SEMI";
    }

    private static int stateColor(String state) {
        return switch (state) {
            case "RELOAD", "CHAMBER" -> RELOAD;
            case "EMPTY" -> EMPTY;
            default -> READY;
        };
    }

    private static void drawScaledString(GuiGraphics graphics, Font font, String text, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, text, Math.round(x / scale), Math.round(y / scale), color, false);
        graphics.pose().popPose();
    }

    private static int lowColor(int baseColor, float ratio) {
        float redAmount = Mth.clamp((0.45f - ratio) / 0.45f, 0.0f, 1.0f);
        return lerpColor(baseColor, EMPTY, redAmount);
    }

    private static int lerpColor(int from, int to, float amount) {
        int red = Mth.lerpInt(amount, from >> 16 & 255, to >> 16 & 255);
        int green = Mth.lerpInt(amount, from >> 8 & 255, to >> 8 & 255);
        int blue = Mth.lerpInt(amount, from & 255, to & 255);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
    }
}
