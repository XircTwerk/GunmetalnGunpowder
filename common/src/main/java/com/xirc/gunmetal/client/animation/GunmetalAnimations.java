package com.xirc.gunmetal.client.animation;

import com.xirc.gunmetal.Gunmetal;
import com.xirc.gunmetal.common.item.AbstractGunItem;
import com.xirc.gunmetal.common.item.AmmoType;
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
import mod.azure.azurelib.AzureLib;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    // Player reload animations cached per source file (e.g. "beretta", "rifle").
    private static final Map<String, Map<String, KeyframeAnimation>> LOCAL_ANIMATIONS = new HashMap<>();
    private static final Map<AbstractClientPlayer, GunAnimation> PLAYER_ANIMATIONS = new WeakHashMap<>();
    private static final Map<AbstractClientPlayer, Float> SIDE_AIM = new WeakHashMap<>();
    private static long lastMainHandSequence = Long.MIN_VALUE;
    private static long lastOffHandSequence = Long.MIN_VALUE;
    private static UUID lastMainHandStackId;
    private static UUID lastOffHandStackId;
    private static String activeMainHandAnimation = "";
    private static String activeOffHandAnimation = "";
    private static int mainHandAnimationTicks;
    private static int offHandAnimationTicks;
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
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            boolean hasMainHandGun = mainHand.getItem() instanceof AbstractGunItem;
            boolean hasOffHandGun = offHand.getItem() instanceof AbstractGunItem;
            if (!hasMainHandGun && !hasOffHandGun) {
                resetMainHandAnimation();
                resetOffHandAnimation();
                SIDE_AIM.remove(player);
                stopPlayerAnimation(player);
                return;
            }
            updateSideAim(player);

            updateHandAnimation(player, mainHand, hasMainHandGun, true);
            updateHandAnimation(player, offHand, hasOffHandGun, false);
            tickActiveAnimations(player);
        });
    }

    private static AdjustmentModifier gunHoldModifier(AbstractClientPlayer player) {
        return new GunArmModifier(partName -> {
            boolean rightArm = "rightArm".equals(partName) || "right_arm".equals(partName);
            boolean leftArm = "leftArm".equals(partName) || "left_arm".equals(partName);
            if (!rightArm && !leftArm) {
                return Optional.empty();
            }
            boolean hasMainHandGun = player.getMainHandItem().getItem() instanceof AbstractGunItem;
            boolean hasOffHandGun = player.getOffhandItem().getItem() instanceof AbstractGunItem;
            if ((rightArm && !hasMainHandGun) || (leftArm && !hasOffHandGun)) {
                return Optional.empty();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
                return Optional.empty();
            }
            if (isAnyReloadAnimationActive()) {
                return Optional.empty();
            }
            float pitchAim = AIM_DOWN_BIAS + (float) Math.toRadians(player.getXRot());
            float sideAim = SIDE_AIM.computeIfAbsent(player, GunmetalAnimations::targetSideAim);
            return Optional.of(new AdjustmentModifier.PartModifier(
                    new Vec3f(ARM_HOLD_ROTATION + pitchAim + fireRecoil(rightArm), sideAim, 0.0f),
                    new Vec3f(0.0f, 0.0f, 0.0f)));
        });
    }

    private static void updateHandAnimation(AbstractClientPlayer player, ItemStack stack, boolean hasGun, boolean mainHand) {
        if (!hasGun) {
            if (mainHand) {
                resetMainHandAnimation();
            } else {
                resetOffHandAnimation();
            }
            return;
        }

        long sequence = stack.getOrCreateTag().getLong(AbstractGunItem.ANIMATION_SEQUENCE_ID);
        UUID stackId = stackId(stack);
        if (stackChanged(mainHand, stackId)) {
            setLastStackId(mainHand, stackId);
            setLastSequence(mainHand, sequence);
            clearActiveAnimation(mainHand);
            return;
        }

        long lastSequence = mainHand ? lastMainHandSequence : lastOffHandSequence;
        if (lastSequence == Long.MIN_VALUE) {
            setLastStackId(mainHand, stackId);
            setLastSequence(mainHand, sequence);
            return;
        }

        if (sequence == lastSequence) {
            return;
        }

        setLastSequence(mainHand, sequence);
        String animation = stack.getOrCreateTag().getString(AbstractGunItem.ANIMATION_ID);
        String previous = mainHand ? activeMainHandAnimation : activeOffHandAnimation;
        int previousTicks = mainHand ? mainHandAnimationTicks : offHandAnimationTicks;
        if (isFireAnimation(animation)) {
            setActiveAnimation(mainHand, animation, FIRE_ANIMATION_TICKS);
            stopPlayerAnimation(player);
        } else if (isReloadAnimation(animation)) {
            // Reload is dispatched as a sequence of parts; keep the single arm clip playing
            // across parts instead of restarting it on each part bump.
            boolean continuingReload = isReloadAnimation(previous) && previousTicks > 0;
            setActiveAnimation(mainHand, animation, reloadAnimationTicks(animation));
            if (!continuingReload) {
                stopPlayerAnimation(player);
                playPlayerAnimation(player, animation, playerAnimationFile(stack));
            }
        }
    }

    private static void setLastSequence(boolean mainHand, long sequence) {
        if (mainHand) {
            lastMainHandSequence = sequence;
        } else {
            lastOffHandSequence = sequence;
        }
    }

    private static void setActiveAnimation(boolean mainHand, String animation, int ticks) {
        if (mainHand) {
            activeMainHandAnimation = animation;
            mainHandAnimationTicks = ticks;
        } else {
            activeOffHandAnimation = animation;
            offHandAnimationTicks = ticks;
        }
    }

    private static void resetMainHandAnimation() {
        lastMainHandSequence = Long.MIN_VALUE;
        lastMainHandStackId = null;
        activeMainHandAnimation = "";
        mainHandAnimationTicks = 0;
    }

    private static void resetOffHandAnimation() {
        lastOffHandSequence = Long.MIN_VALUE;
        lastOffHandStackId = null;
        activeOffHandAnimation = "";
        offHandAnimationTicks = 0;
    }

    private static void setLastStackId(boolean mainHand, UUID stackId) {
        if (mainHand) {
            lastMainHandStackId = stackId;
        } else {
            lastOffHandStackId = stackId;
        }
    }

    private static boolean stackChanged(boolean mainHand, UUID stackId) {
        UUID lastStackId = mainHand ? lastMainHandStackId : lastOffHandStackId;
        return stackId != null && !stackId.equals(lastStackId);
    }

    private static UUID stackId(ItemStack stack) {
        return stack.getOrCreateTag().hasUUID(AzureLib.ITEM_UUID_TAG)
                ? stack.getOrCreateTag().getUUID(AzureLib.ITEM_UUID_TAG)
                : null;
    }

    private static void updateSideAim(AbstractClientPlayer player) {
        float target = targetSideAim(player);
        float current = SIDE_AIM.getOrDefault(player, target);
        SIDE_AIM.put(player, current + (target - current) * SIDE_AIM_SMOOTHING);
    }

    private static float targetSideAim(AbstractClientPlayer player) {
        return (float) Math.toRadians(Mth.clamp(Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot), -MAX_SIDE_AIM, MAX_SIDE_AIM));
    }

    private static void playPlayerAnimation(AbstractClientPlayer player, String animationName, String file) {
        GunAnimation holder = getGunAnimation(player);
        KeyframeAnimation animation = findPlayerAnimation(animationName, file);
        if (holder == null || animation == null) {
            return;
        }

        holder.play(animationName, animation);
    }

    private static KeyframeAnimation findPlayerAnimation(String animationName, String file) {
        Map<String, KeyframeAnimation> animations = loadLocalAnimations(file);
        KeyframeAnimation animation = animations.get(animationName);
        return animation != null ? animation : animations.get(playerAnimationFallback(animationName));
    }

    /** Player-animation source file for a gun: rifle ammo uses rifle.json, everything else beretta.json. */
    private static String playerAnimationFile(ItemStack stack) {
        if (stack.getItem() instanceof AbstractGunItem gun && gun.getAmmoType() == AmmoType.RIFLE) {
            return "rifle";
        }
        return "beretta";
    }

    private static Map<String, KeyframeAnimation> loadLocalAnimations(String file) {
        Map<String, KeyframeAnimation> cached = LOCAL_ANIMATIONS.get(file);
        if (cached != null) {
            return cached;
        }

        Map<String, KeyframeAnimation> animations = new HashMap<>();
        LOCAL_ANIMATIONS.put(file, animations);

        Optional<net.minecraft.server.packs.resources.Resource> resource = Minecraft.getInstance()
                .getResourceManager()
                .getResource(Gunmetal.id("player_animations/" + file + ".json"));
        if (resource.isEmpty()) {
            return animations;
        }

        try (InputStream stream = resource.get().open()) {
            for (KeyframeAnimation animation : AnimationSerializing.deserializeAnimation(stream)) {
                Object name = animation.extraData.get("name");
                if (name != null) {
                    animations.put(name.toString(), animation);
                }
            }
        } catch (IOException ignored) {
        }
        return animations;
    }

    private static void tickActiveAnimations(AbstractClientPlayer player) {
        mainHandAnimationTicks = tickActiveAnimation(player, activeMainHandAnimation, mainHandAnimationTicks, true);
        offHandAnimationTicks = tickActiveAnimation(player, activeOffHandAnimation, offHandAnimationTicks, false);
    }

    private static int tickActiveAnimation(AbstractClientPlayer player, String animation, int ticks, boolean mainHand) {
        if (ticks <= 0) {
            clearActiveAnimation(mainHand);
            return 0;
        }

        int remaining = ticks - 1;
        if (remaining > 0) {
            return remaining;
        }

        if (isReloadAnimation(animation)) {
            stopPlayerAnimation(player);
        }
        clearActiveAnimation(mainHand);
        return 0;
    }

    private static void clearActiveAnimation(boolean mainHand) {
        if (mainHand) {
            activeMainHandAnimation = "";
        } else {
            activeOffHandAnimation = "";
        }
    }

    private static float fireRecoil(boolean mainHand) {
        String animation = mainHand ? activeMainHandAnimation : activeOffHandAnimation;
        int ticks = mainHand ? mainHandAnimationTicks : offHandAnimationTicks;
        if (!isFireAnimation(animation) || ticks <= 0) {
            return 0.0f;
        }
        float age = FIRE_ANIMATION_TICKS - ticks;
        float progress = Math.max(0.0f, Math.min(1.0f, age / (float) FIRE_ANIMATION_TICKS));
        return (float) (-Math.sin(progress * Math.PI) * 0.25f);
    }

    private static boolean isAnyReloadAnimationActive() {
        return (isReloadAnimation(activeMainHandAnimation) && mainHandAnimationTicks > 0)
                || (isReloadAnimation(activeOffHandAnimation) && offHandAnimationTicks > 0);
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
        public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
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
        public @NotNull FirstPersonMode getFirstPersonMode(float tickDelta) {
            return FirstPersonMode.NONE;
        }

        @Override
        public @NotNull FirstPersonConfiguration getFirstPersonConfiguration(float tickDelta) {
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
        return animation.startsWith("reload") || animation.startsWith("deload");
    }

    private static int reloadAnimationTicks(String animation) {
        return "deload".equals(animation) ? DELOAD_ANIMATION_TICKS : RELOAD_EMPTY_ANIMATION_TICKS;
    }

    private static String playerAnimationFallback(String animation) {
        return isReloadAnimation(animation) ? "reload" : animation;
    }

    private static final class GunArmModifier extends AdjustmentModifier {
        GunArmModifier(java.util.function.Function<String, Optional<PartModifier>> source) {
            super(source);
        }

        @Override
        protected Vec3f transformVector(Vec3f vector, TransformType type, PartModifier partModifier, float fade) {
            return switch (type) {
                case POSITION, BEND -> vector;
                case ROTATION -> partModifier.rotation().scale(fade);
            };
        }
    }
}
