package com.xirc.militech.client.hud;

import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.common.item.AmmoBoxItem;
import com.xirc.militech.common.item.AmmoType;
import com.xirc.militech.registry.MilitechGuns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class GunHud {
    private static final int WIDTH = 136;
    private static final int HEIGHT = 52;
    private static final int PADDING = 5;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int HOTBAR_GAP = 8;
    private static final int SCREEN_MARGIN = 1;
    private static final int HUD_RIGHT_OFFSET = 10;
    private static final int HUD_LEFT_OFFSET = 34; // for offhand weapon
    private static final int HUD_DOWN_OFFSET = 7;
    private static final int AMMO_WIDTH = 78;
    private static final float STATE_SCALE = 0.75f;
    private static final float PREVIEW_MODEL_SCALE = 16.0f;
    private static final float ASSAULT_RIFLE_PREVIEW_MODEL_SCALE = 9.5f;

    private static final int TEXT = 0xFFE8E0CF;
    private static final int MUTED = 0xFF9C9485;
    private static final int AMMO = 0xFFFFE08A;
    private static final int EMPTY = 0xFFFF6F5F;
    private static final int READY = 0xFFBFEA8A;
    private static final int RELOAD = 0xFFFFB347;
    private static final int BAR_BACK = 0xFF2B2B2B;
    private static final int BAR_BORDER = 0xFF050505;
    private static final int BAR_GOOD = 0xFF63D66E;

    private GunHud() {
    }

    public static void init() {
    }

    public static void renderAboveToasts(GuiGraphics graphics) {
        render(graphics);
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        int rightX = Math.min(graphics.guiWidth() - WIDTH - SCREEN_MARGIN,
                graphics.guiWidth() / 2 + HOTBAR_HALF_WIDTH + HOTBAR_GAP + HUD_RIGHT_OFFSET);
        int y = graphics.guiHeight() - HEIGHT - SCREEN_MARGIN + HUD_DOWN_OFFSET;
        int leftX = Math.max(SCREEN_MARGIN,
                graphics.guiWidth() / 2 - HOTBAR_HALF_WIDTH - HOTBAR_GAP - HUD_LEFT_OFFSET - WIDTH);

        if (mainHand.getItem() instanceof AbstractGunItem mainGun) {
            drawGunCard(graphics, minecraft.font, player, mainGun, mainHand, rightX, y, false);
        }
        if (offHand.getItem() instanceof AbstractGunItem offGun) {
            drawGunCard(graphics, minecraft.font, player, offGun, offHand, leftX, y, true);
        }
    }

    private static void drawGunCard(GuiGraphics graphics, Font font, Player player, AbstractGunItem gun, ItemStack stack, int x, int y, boolean mirrored) {
        if (mirrored) {
            drawAmmo(graphics, font, player, gun, stack, x + PADDING, y + PADDING);
            drawPreview(graphics, stack, x + WIDTH - PADDING - 35, y + PADDING);
            drawDurability(graphics, stack, x + WIDTH - PADDING - 36, y + 39, 34, 5);
            return;
        }

        drawPreview(graphics, stack, x + PADDING + 2, y + PADDING);
        drawDurability(graphics, stack, x + PADDING + 1, y + 39, 34, 5);
        drawAmmo(graphics, font, player, gun, stack, x + 46, y + PADDING);
    }

    private static void drawPreview(GuiGraphics graphics, ItemStack stack, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        var bufferSource = minecraft.renderBuffers().bufferSource();
        float scale = previewModelScale(stack);

        graphics.pose().pushPose();
        graphics.pose().translate(x + 16.0f, y + 16.0f, 150.0f);
        graphics.pose().mulPoseMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        graphics.pose().scale(scale, scale, scale);
        minecraft.getItemRenderer().renderStatic(
                stack, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                graphics.pose(), bufferSource, minecraft.level, 0);
        bufferSource.endBatch();
        graphics.pose().popPose();
    }

    private static float previewModelScale(ItemStack stack) {
        return stack.is(MilitechGuns.ASSAULT_RIFLE.get())
                ? ASSAULT_RIFLE_PREVIEW_MODEL_SCALE
                : PREVIEW_MODEL_SCALE;
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
        int reserve = reserveAmmo(player, gun);
        String name = stack.getHoverName().getString();
        String ammoType = gun.getAmmoItem().getDescription().getString();
        String state = state(player, gun, stack, loaded);
        int stateColor = stateColor(state);
        float ammoRatio = max <= 0 ? 0.0f : Mth.clamp(loaded / (float) max, 0.0f, 1.0f);

        graphics.drawString(font, trim(font, name, AMMO_WIDTH - 18), x, y, TEXT, false);
        drawScaledString(graphics, font, state, x + AMMO_WIDTH - Math.round(font.width(state) * STATE_SCALE), y + 1, stateColor);

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

    private static int reserveAmmo(Player player, AbstractGunItem gun) {
        if (player.isCreative()) {
            return 999;
        }

        Item ammoItem = gun.getAmmoItem();
        AmmoType ammoType = gun.getAmmoType();
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ammoItem) {
                count += stack.getCount();
            } else if (stack.getItem() instanceof AmmoBoxItem box && box.getAmmoType() == ammoType) {
                count += box.countRounds(stack);
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
        if (AbstractGunItem.isCoolingDown(player, stack)) {
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

    private static void drawScaledString(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(GunHud.STATE_SCALE, GunHud.STATE_SCALE, 1.0f);
        graphics.drawString(font, text, Math.round(x / GunHud.STATE_SCALE), Math.round(y / GunHud.STATE_SCALE), color, false);
        graphics.pose().popPose();
    }

    private static int lowColor(int baseColor, float ratio) {
        float redAmount = Mth.clamp((0.45f - ratio) / 0.45f, 0.0f, 1.0f);
        return lerpColor(baseColor, redAmount);
    }

    private static int lerpColor(int from, float amount) {
        int red = Mth.lerpInt(amount, from >> 16 & 255, GunHud.EMPTY >> 16 & 255);
        int green = Mth.lerpInt(amount, from >> 8 & 255, GunHud.EMPTY >> 8 & 255);
        int blue = Mth.lerpInt(amount, from & 255, GunHud.EMPTY & 255);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
    }
}
