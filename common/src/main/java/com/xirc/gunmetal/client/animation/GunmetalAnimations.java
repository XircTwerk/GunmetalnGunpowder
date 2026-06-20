package com.xirc.gunmetal.client.animation;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class GunmetalAnimations {
    private static final ResourceLocation CONTROLLER_ID = Gunmetal.id("animation_controller");
    private static final ResourceLocation BERETTA_PLAYER_ANIMATION = Gunmetal.id("beretta");
    private static final FirstPersonConfiguration FIRST_PERSON_CONFIG =
            new FirstPersonConfiguration(true, true, true, true);

    private static long lastMainHandSequence = Long.MIN_VALUE;
    private static boolean initialized;

    private GunmetalAnimations() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                CONTROLLER_ID,
                1001,
                player -> {
                    ModifierLayer<KeyframeAnimationPlayer> layer = new ModifierLayer<>();
                    layer.addModifierBefore(gunHoldModifier(player));
                    return layer;
                });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (minecraft.player == null) {
                return;
            }
            AbstractClientPlayer player = minecraft.player;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof AbstractGunItem)) {
                lastMainHandSequence = Long.MIN_VALUE;
                return;
            }

            long sequence = stack.getOrCreateTag().getLong(AbstractGunItem.ANIMATION_SEQUENCE_ID);
            if (sequence == lastMainHandSequence) {
                return;
            }
            lastMainHandSequence = sequence;

            String animation = stack.getOrCreateTag().getString(AbstractGunItem.ANIMATION_ID);
            if ("fire".equals(animation) || "reload".equals(animation)) {
                playAnimation(player);
            }
        });
    }

    private static AdjustmentModifier gunHoldModifier(AbstractClientPlayer player) {
        return new AdjustmentModifier(partName -> {
            if (!(player.getMainHandItem().getItem() instanceof AbstractGunItem)) {
                return Optional.empty();
            }
            if (isAnimationPlaying(player)) {
                return Optional.empty();
            }
            if ("rightArm".equals(partName) || "right_arm".equals(partName)) {
                return Optional.of(new AdjustmentModifier.PartModifier(
                        new Vec3f(-1.5708f, 0.0f, 0.0f),
                        new Vec3f(0.0f, 0.0f, 0.0f)));
            }
            return Optional.empty();
        });
    }

    @SuppressWarnings("unchecked")
    private static void playAnimation(AbstractClientPlayer player) {
        ModifierLayer<KeyframeAnimationPlayer> layer = getLayer(player);
        KeyframeAnimation animation = PlayerAnimationRegistry.getAnimation(BERETTA_PLAYER_ANIMATION);
        if (layer == null || animation == null) {
            return;
        }

        layer.setAnimation(new KeyframeAnimationPlayer(animation)
                .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                .setFirstPersonConfiguration(FIRST_PERSON_CONFIG));
    }

    private static boolean isAnimationPlaying(AbstractClientPlayer player) {
        ModifierLayer<KeyframeAnimationPlayer> layer = getLayer(player);
        return layer != null && layer.getAnimation() != null && layer.getAnimation().isActive();
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<KeyframeAnimationPlayer> getLayer(AbstractClientPlayer player) {
        Object animation = PlayerAnimationAccess.getPlayerAssociatedData(player).get(CONTROLLER_ID);
        if (animation instanceof ModifierLayer<?> layer) {
            return (ModifierLayer<KeyframeAnimationPlayer>) layer;
        }
        return null;
    }
}
