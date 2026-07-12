package com.xirc.militech.client.animation;

import com.xirc.militech.Militech;
import com.xirc.militech.common.item.AbstractMeleeItem;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
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
public final class MilitechMeleeAnimations {
    private static final ResourceLocation CONTROLLER_ID = Militech.id("melee_animation_controller");
    private static final FirstPersonConfiguration FIRST_PERSON_CONFIG = new FirstPersonConfiguration(true, true, true, true);

    // Player-animation clips cached per source file (e.g. "knife").
    private static final Map<String, Map<String, KeyframeAnimation>> LOCAL_ANIMATIONS = new HashMap<>();
    private static final Map<AbstractClientPlayer, MeleeAnimation> PLAYER_ANIMATIONS = new WeakHashMap<>();
    private static long lastSequence = Long.MIN_VALUE;
    private static UUID lastStackId;
    private static boolean initialized;

    private MilitechMeleeAnimations() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                CONTROLLER_ID,
                1002,
                player -> {
                    MeleeAnimation animation = new MeleeAnimation();
                    PLAYER_ANIMATIONS.put(player, animation);
                    return new ModifierLayer<>(animation);
                });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (minecraft.player == null) {
                return;
            }
            AbstractClientPlayer player = minecraft.player;
            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof AbstractMeleeItem melee)) {
                lastSequence = Long.MIN_VALUE;
                lastStackId = null;
                stopPlayerAnimation(player);
                return;
            }
            updateAnimation(player, mainHand, melee);
        });
    }

    private static void updateAnimation(AbstractClientPlayer player, ItemStack stack, AbstractMeleeItem melee) {
        long sequence = stack.getOrCreateTag().getLong(AbstractMeleeItem.ANIMATION_SEQUENCE_ID);
        UUID stackId = stackId(stack);
        if (stackId != null && !stackId.equals(lastStackId)) {
            lastStackId = stackId;
            lastSequence = sequence;
            return;
        }
        if (lastSequence == Long.MIN_VALUE) {
            lastStackId = stackId;
            lastSequence = sequence;
            return;
        }
        if (sequence == lastSequence) {
            return;
        }
        lastSequence = sequence;
        String animation = stack.getOrCreateTag().getString(AbstractMeleeItem.ANIMATION_ID);
        playPlayerAnimation(player, animation, melee.playerAnimationFile());
    }

    private static void playPlayerAnimation(AbstractClientPlayer player, String animationName, String file) {
        MeleeAnimation holder = getMeleeAnimation(player);
        KeyframeAnimation animation = loadLocalAnimations(file).get(animationName);
        if (holder == null || animation == null) {
            return;
        }
        holder.play(animation);
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
                .getResource(Militech.id("player_animations/" + file + ".json"));
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

    private static void stopPlayerAnimation(AbstractClientPlayer player) {
        MeleeAnimation animation = getMeleeAnimation(player);
        if (animation != null) {
            animation.stop();
        }
    }

    private static MeleeAnimation getMeleeAnimation(AbstractClientPlayer player) {
        MeleeAnimation animation = PLAYER_ANIMATIONS.get(player);
        if (animation != null) {
            return animation;
        }
        Object layerAnimation = PlayerAnimationAccess.getPlayerAssociatedData(player).get(CONTROLLER_ID);
        if (layerAnimation instanceof ModifierLayer<?> layer) {
            IAnimation base = layer.getAnimation();
            if (base instanceof MeleeAnimation meleeAnimation) {
                PLAYER_ANIMATIONS.put(player, meleeAnimation);
                return meleeAnimation;
            }
        }
        return null;
    }

    private static UUID stackId(ItemStack stack) {
        return stack.getOrCreateTag().hasUUID(AzureLib.ITEM_UUID_TAG)
                ? stack.getOrCreateTag().getUUID(AzureLib.ITEM_UUID_TAG)
                : null;
    }

    private static final class MeleeAnimation implements IAnimation {
        private KeyframeAnimationPlayer triggered;

        void play(KeyframeAnimation animation) {
            triggered = new KeyframeAnimationPlayer(animation)
                    .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                    .setFirstPersonConfiguration(FIRST_PERSON_CONFIG);
        }

        void stop() {
            triggered = null;
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
                return triggered.get3DTransform(animationPartName(modelName), type, tickDelta, value0);
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
}
