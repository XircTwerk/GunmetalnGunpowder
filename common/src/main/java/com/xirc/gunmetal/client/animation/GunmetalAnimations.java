package com.xirc.gunmetal.client.animation;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public final class GunmetalAnimations {
    private static final ResourceLocation CONTROLLER_ID = Gunmetal.id("animation_controller");
    private static final FirstPersonConfiguration FIRST_PERSON_CONFIG = new FirstPersonConfiguration(true, true, true, true);
    private static final int FIRE_ANIMATION_TICKS = 6;
    private static final int RELOAD_EMPTY_ANIMATION_TICKS = 17;
    private static final int DELOAD_ANIMATION_TICKS = 34;
    private static final float ARM_HOLD_ROTATION = -1.5708f;
    private static final float AIM_DOWN_BIAS = 0.22f;
    private static final float MAX_SIDE_AIM = 75.0f;
    private static final float SIDE_AIM_SMOOTHING = 0.35f;

    private static final Map<String, KeyframeAnimation> LOCAL_ANIMATIONS = new HashMap<>();
    private static final Map<AbstractClientPlayer, GunAnimation> PLAYER_ANIMATIONS = new WeakHashMap<>();
    private static final Map<AbstractClientPlayer, Float> SIDE_AIM = new WeakHashMap<>();
    private static long lastMainHandSequence = Long.MIN_VALUE;
    private static String activeAnimation = "";
    private static int animationTicks;
    private static boolean localAnimationsLoaded;
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
                    GunAnimation animation = new GunAnimation();
                    PLAYER_ANIMATIONS.put(player, animation);
                    ModifierLayer<IAnimation> layer = new ModifierLayer<>(animation);
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
                activeAnimation = "";
                animationTicks = 0;
                SIDE_AIM.remove(player);
                stopPlayerAnimation(player);
                return;
            }
            updateSideAim(player);

            long sequence = stack.getOrCreateTag().getLong(AbstractGunItem.ANIMATION_SEQUENCE_ID);
            if (lastMainHandSequence == Long.MIN_VALUE) {
                lastMainHandSequence = sequence;
                tickActiveAnimation(player);
                return;
            }

            if (sequence != lastMainHandSequence) {
                lastMainHandSequence = sequence;

                String animation = stack.getOrCreateTag().getString(AbstractGunItem.ANIMATION_ID);
                if (isFireAnimation(animation)) {
                    activeAnimation = animation;
                    animationTicks = FIRE_ANIMATION_TICKS;
                    stopPlayerAnimation(player);
                } else if (isReloadAnimation(animation)) {
                    activeAnimation = animation;
                    animationTicks = reloadAnimationTicks(animation);
                    stopPlayerAnimation(player);
                    playPlayerAnimation(player, animation);
                }
            }

            tickActiveAnimation(player);
        });
    }

    private static AdjustmentModifier gunHoldModifier(AbstractClientPlayer player) {
        return new AdjustmentModifier(partName -> {
            if (!(player.getMainHandItem().getItem() instanceof AbstractGunItem)) {
                return Optional.empty();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
                return Optional.empty();
            }
            if (isReloadAnimation(activeAnimation) && animationTicks > 0) {
                return Optional.empty();
            }
            if ("rightArm".equals(partName) || "right_arm".equals(partName)) {
                float pitchAim = AIM_DOWN_BIAS + (float) Math.toRadians(player.getXRot());
                float sideAim = SIDE_AIM.computeIfAbsent(player, GunmetalAnimations::targetSideAim);
                return Optional.of(new AdjustmentModifier.PartModifier(
                        new Vec3f(ARM_HOLD_ROTATION + pitchAim + fireRecoil(), sideAim, 0.0f),
                        new Vec3f(0.0f, 0.0f, 0.0f)));
            }
            return Optional.empty();
        });
    }

    private static void updateSideAim(AbstractClientPlayer player) {
        float target = targetSideAim(player);
        float current = SIDE_AIM.getOrDefault(player, target);
        SIDE_AIM.put(player, current + (target - current) * SIDE_AIM_SMOOTHING);
    }

    private static float targetSideAim(AbstractClientPlayer player) {
        return (float) Math.toRadians(Mth.clamp(Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot), -MAX_SIDE_AIM, MAX_SIDE_AIM));
    }

    private static void playPlayerAnimation(AbstractClientPlayer player, String animationName) {
        GunAnimation holder = getGunAnimation(player);
        KeyframeAnimation animation = findPlayerAnimation(animationName);
        if (holder == null || animation == null) {
            return;
        }

        holder.play(animationName, animation);
    }

    private static KeyframeAnimation findPlayerAnimation(String animationName) {
        KeyframeAnimation animation = PlayerAnimationRegistry.getAnimation(Gunmetal.id(animationName));
        if (animation != null) {
            return animation;
        }

        loadLocalAnimations();
        animation = LOCAL_ANIMATIONS.get(animationName);
        return animation != null ? animation : LOCAL_ANIMATIONS.get(playerAnimationFallback(animationName));
    }

    private static void loadLocalAnimations() {
        if (localAnimationsLoaded) {
            return;
        }
        localAnimationsLoaded = true;

        Optional<net.minecraft.server.packs.resources.Resource> resource = Minecraft.getInstance()
                .getResourceManager()
                .getResource(Gunmetal.id("player_animations/beretta.json"));
        if (resource.isEmpty()) {
            return;
        }

        try (InputStream stream = resource.get().open()) {
            for (KeyframeAnimation animation : AnimationSerializing.deserializeAnimation(stream)) {
                Object name = animation.extraData.get("name");
                if (name != null) {
                    LOCAL_ANIMATIONS.put(name.toString(), animation);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void tickActiveAnimation(AbstractClientPlayer player) {
        if (animationTicks > 0) {
            animationTicks--;
            if (animationTicks > 0) {
                return;
            }
            if (isReloadAnimation(activeAnimation)) {
                stopPlayerAnimation(player);
            }
            activeAnimation = "";
        } else {
            activeAnimation = "";
        }
    }

    private static float fireRecoil() {
        if (!isFireAnimation(activeAnimation) || animationTicks <= 0) {
            return 0.0f;
        }
        float age = FIRE_ANIMATION_TICKS - animationTicks;
        float progress = Math.max(0.0f, Math.min(1.0f, age / (float) FIRE_ANIMATION_TICKS));
        return (float) (-Math.sin(progress * Math.PI) * 0.25f);
    }

    private static void stopPlayerAnimation(AbstractClientPlayer player) {
        GunAnimation animation = getGunAnimation(player);
        if (animation != null) {
            animation.stop();
        }
    }

    private static GunAnimation getGunAnimation(AbstractClientPlayer player) {
        GunAnimation animation = PLAYER_ANIMATIONS.get(player);
        if (animation != null) {
            return animation;
        }
        Object layerAnimation = PlayerAnimationAccess.getPlayerAssociatedData(player).get(CONTROLLER_ID);
        if (layerAnimation instanceof ModifierLayer<?> layer) {
            IAnimation base = layer.getAnimation();
            if (base instanceof GunAnimation gunAnimation) {
                PLAYER_ANIMATIONS.put(player, gunAnimation);
                return gunAnimation;
            }
        }
        return null;
    }

    private static final class GunAnimation implements IAnimation {
        private KeyframeAnimationPlayer triggered;
        private String triggeredName = "";

        void play(String animationName, KeyframeAnimation animation) {
            triggeredName = animationName;
            triggered = new KeyframeAnimationPlayer(animation)
                    .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                    .setFirstPersonConfiguration(FIRST_PERSON_CONFIG);
        }

        void stop() {
            triggered = null;
            triggeredName = "";
        }

        boolean isTriggeredActive() {
            return triggered != null && triggered.isActive();
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
            if (isTriggeredActive()) {
                String partName = animationPartName(modelName);
                Vec3f transform = triggered.get3DTransform(partName, type, tickDelta, value0);
                if (isFireAnimation(triggeredName)
                        && "rightArm".equals(partName)
                        && type == TransformType.ROTATION) {
                    return new Vec3f(transform.getX() - ARM_HOLD_ROTATION, transform.getY(), transform.getZ());
                }
                return transform;
            }
            return value0;
        }

        @Override
        public void setupAnim(float tickDelta) {
            if (isTriggeredActive()) {
                triggered.setupAnim(tickDelta);
            }
        }

        @Override
        public void tick() {
            if (isTriggeredActive()) {
                triggered.tick();
            }
        }

        @Override
        public FirstPersonMode getFirstPersonMode(float tickDelta) {
            if (isReloadAnimation(triggeredName) && isTriggeredActive()) {
                return FirstPersonMode.THIRD_PERSON_MODEL;
            }
            return FirstPersonMode.NONE;
        }

        @Override
        public FirstPersonConfiguration getFirstPersonConfiguration(float tickDelta) {
            return FIRST_PERSON_CONFIG;
        }

        private static String animationPartName(String modelName) {
            return switch (modelName) {
                case "right_arm" -> "rightArm";
                case "left_arm" -> "leftArm";
                default -> modelName;
            };
        }
    }

    private static boolean isFireAnimation(String animation) {
        return "fire".equals(animation) || "fire_final".equals(animation);
    }

    private static boolean isReloadAnimation(String animation) {
        return "reload".equals(animation) || "reload_empty".equals(animation) || "deload".equals(animation);
    }

    private static int reloadAnimationTicks(String animation) {
        return "deload".equals(animation) ? DELOAD_ANIMATION_TICKS : RELOAD_EMPTY_ANIMATION_TICKS;
    }

    private static String playerAnimationFallback(String animation) {
        return isReloadAnimation(animation) ? "reload" : animation;
    }
}
