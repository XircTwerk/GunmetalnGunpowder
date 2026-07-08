package com.xirc.milicraft.common.item;

import com.xirc.milicraft.common.data.gun.GunStats;
import com.xirc.milicraft.common.data.gun.GunStatsManager;
import com.xirc.milicraft.common.entity.projectile.BulletProjectile;
import com.xirc.milicraft.common.system.GunAiming;
import com.xirc.milicraft.common.system.hitscan.HitscanGunShot;
import com.xirc.milicraft.common.tickable.GunReloadQueue;
import com.xirc.milicraft.common.util.DimensionData;
import com.xirc.milicraft.registry.MilicraftItems;
import com.xirc.milicraft.registry.MilicraftSoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractGunItem extends Item {
    public static final String SHOTS_ID = "Shots";
    public static final String RELOADING_ID = "Reloading";
    public static final String ANIMATION_ID = "MilicraftGunAnimation";
    public static final String ANIMATION_SEQUENCE_ID = "MilicraftGunAnimationSequence";
    public static final String COOLDOWN_UNTIL_ID = "MilicraftGunCooldownUntil";

    protected AbstractGunItem(Properties settings) {
        super(settings);
    }

    protected abstract ResourceLocation statsId();

    protected abstract GunStats defaultStats();

    protected GunStats stats() {
        return GunStatsManager.get(statsId()).orElse(defaultStats());
    }

    public GunStats getStats() {
        return stats();
    }

    /**
     * Maximum ammo stored in this gun's item NBT.
     * <p>
     * This controls the ammo tooltip, durability-style ammo bar, default loaded ammo,
     * and when the reload queue stops loading more rounds.
     */
    protected int maxRounds() {
        return stats().maxRounds();
    }

    public int getMaxRounds() {
        return maxRounds();
    }

    /**
     * Base damage dealt by each hitscan shot before range falloff is applied.
     * <p>
     * The visual bullet entity is currently spawned with zero damage, so this value is
     * applied by {@link HitscanGunShot} instead of by the projectile.
     */
    protected float damage() {
        return stats().damage();
    }

    /**
     * Maximum hitscan distance in blocks.
     * <p>
     * Shots trace from the shooter's eye position up to this range, stopping early if
     * a solid block clips the ray. Damage also falls from normal damage at 8 blocks
     * to zero damage at this distance.
     */
    protected float range() {
        return stats().range();
    }

    /**
     * Knockback strength applied to living targets hit by the hitscan shot.
     * <p>
     * Higher values push targets farther away from the shooter.
     */
    protected float knockback() {
        return stats().knockback();
    }

    /**
     * Number of barrel groups fired per shot.
     * <p>
     * Total hitscan rays are {@code barrels() * pelletsPerBarrel()}.
     */
    protected int barrels() {
        return stats().barrels();
    }

    /**
     * Number of hitscan rays fired by each barrel group.
     * <p>
     * Use {@code 1} for a single bullet. Higher values act like pellets.
     */
    protected int pelletsPerBarrel() {
        return stats().pelletsPerBarrel();
    }

    /**
     * Random aim spread applied to each hitscan ray before normalizing.
     * <p>
     * Use {@code 0.0f} for a straight shot.
     */
    protected float spread() {
        return stats().spread();
    }

    /**
     * Bullet diameter used by the visual projectile, measured in millimeters.
     * <p>
     * This feeds the projectile mass calculation for penetration and ricochet behavior.
     */
    protected float caliber() {
        return stats().caliber();
    }

    /**
     * Bullet length used by the visual projectile, measured in millimeters.
     * <p>
     * Together with {@link #caliber()}, this feeds the projectile mass calculation.
     */
    protected float bulletLength() {
        return stats().bulletLength();
    }

    /**
     * Stun duration carried by the visual bullet projectile, measured in ticks.
     * <p>
     * The current plain Minecraft implementation stores this value for future combat
     * effects, but does not yet apply a stun effect on hit.
     */
    protected int stunTicks() {
        return stats().stunTicks();
    }

    /**
     * Short cooldown added immediately when the player starts a fire input.
     * <p>
     * This prevents duplicate input handling before the main refire cooldown is applied.
     */
    protected int inputCooldownTicks() {
        return stats().inputCooldownTicks();
    }

    /**
     * Main cooldown after a successful shot, measured in ticks.
     * <p>
     * While this cooldown is active, the gun cannot fire again. If this is shorter
     * than the fire animation, the next accepted shot restarts that animation early.
     * Minecraft runs at 20 ticks per second under normal conditions.
     */
    protected int refireCooldownTicks() {
        return stats().refireCooldownTicks();
    }

    /**
     * Cooldown shown while reload input is active, measured in ticks.
     * <p>
     * This is applied when reload begins. It should usually match the weapon's
     * reload duration so the player cannot shoot before the reload finishes.
     */
    protected int reloadCooldownTicks() {
        return stats().reloadCooldownTicks();
    }

    /**
     * Full reload duration for this weapon, measured in ticks.
     * <p>
     * The reload queue waits this long before calling {@link #finishReload(ItemStack, Level, LivingEntity)}.
     * The actual ammo is committed all at once when that timer ends.
     */
    protected int reloadDurationTicks() {
        return stats().reloadDurationTicks();
    }

    /**
     * Strength used when bullets damage blocks.
     * <p>
     * Transparent blocks break instantly while the {@code bulletsBreakBlocks} gamerule is enabled.
     * Other breakable blocks use a quarter of the gun's regular damage.
     */
    protected float blockDamage() {
        return damage() / 4.0f;
    }

    /**
     * Sound played when this gun fires.
     */
    protected SoundEvent fireSound() {
        return MilicraftSoundRegistry.REVOLVER_FIRE.get();
    }

    /**
     * Sound played when reload starts and when each round is loaded.
     */
    protected SoundEvent reloadSound() {
        return MilicraftSoundRegistry.LOAD.get();
    }

    /**
     * Ammo family this gun fires. Determines which round item feeds it and which
     * ammo boxes it can reload from.
     */
    protected AmmoType ammoType() {
        return AmmoType.PISTOL;
    }

    public AmmoType getAmmoType() {
        return ammoType();
    }

    /**
     * Item consumed for each loaded round unless the player is in creative mode.
     */
    protected Item ammoItem() {
        return MilicraftItems.roundFor(ammoType());
    }

    public Item getAmmoItem() {
        return ammoItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        int shots = getShots(stack);
        tooltip.add(Component.translatable("tooltip.milicraft.gun.ammo")
                .append(Component.literal(" " + shots + "/" + maxRounds()).withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.translatable("tooltip.milicraft.gun.fire").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.milicraft.gun.reload").withStyle(ChatFormatting.GRAY));
        if (isReloading(stack)) {
            tooltip.add(Component.translatable("tooltip.milicraft.gun.reloading").withStyle(ChatFormatting.GOLD));
        }

        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0.0f;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return InteractionResultHolder.fail(user.getItemInHand(hand));
    }

    public boolean tryShoot(Player user, ItemStack itemStack) {
        return tryShoot(user, itemStack, null);
    }

    public boolean tryShoot(Player user, ItemStack itemStack, @Nullable Vec3 muzzle) {
        if (user.isSpectator()) {
            return false;
        }
        return fireFromUse(user.level(), user, itemStack, muzzle).getResult().consumesAction();
    }

    public boolean tryReload(Player user, ItemStack itemStack) {
        if (user.isSpectator()) {
            return false;
        }
        return reloadFromUse(user.level(), user, itemStack).getResult().consumesAction();
    }

    private InteractionResultHolder<ItemStack> fireFromUse(Level world, Player user, ItemStack itemStack, @Nullable Vec3 muzzle) {
        if (!canFire(user, itemStack)) {
            playEmptyFireSound(world, user, itemStack);
            return InteractionResultHolder.fail(itemStack);
        }
        markAnimation(itemStack, fireAnimation(itemStack, user));
        if (!world.isClientSide) {
            addCooldown(user, itemStack, inputCooldownTicks());
            fire(itemStack, world, user, muzzle);
        }
        return InteractionResultHolder.success(itemStack);
    }

    private InteractionResultHolder<ItemStack> reloadFromUse(Level world, Player user, ItemStack itemStack) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        if (shots >= maxRounds()) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (data.getBoolean(RELOADING_ID) && isOnCooldown(user, itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!user.isCreative() && !hasAmmoInInventory(user)) {
            return InteractionResultHolder.fail(itemStack);
        }

        List<ReloadPart> parts = reloadParts(itemStack);
        int reloadTicks = 0;
        for (ReloadPart part : parts) {
            reloadTicks += part.ticks();
        }
        ReloadPart firstPart = parts.get(0);
        markAnimation(itemStack, firstPart.animation());
        if (!world.isClientSide) {
            data.putBoolean(RELOADING_ID, true);
            addCooldown(user, itemStack, reloadTicks);
            GunReloadQueue.enqueue(new DimensionData(user, world.dimension(), reloadTicks, heldHand(user, itemStack), parts));
            if (!hasReloadPartSounds(parts)) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), reloadSound(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            SoundEvent firstPartSound = reloadPartSound(firstPart.animation());
            if (firstPartSound != null) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), firstPartSound, SoundSource.PLAYERS, 1.4f, 1.0f);
            }
        }

        return InteractionResultHolder.success(itemStack);
    }

    public static boolean handleLeftClick(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof AbstractGunItem;
    }

    protected String fireAnimation() {
        return "fire";
    }

    protected String fireAnimation(ItemStack stack, Player player) {
        return fireAnimation();
    }

    protected String reloadAnimation() {
        return "reload";
    }

    protected String reloadAnimation(ItemStack stack) {
        return reloadAnimation();
    }

    protected int reloadDurationTicks(ItemStack stack) {
        return reloadDurationTicks();
    }

    /**
     * Ordered reload segments for this gun. The reload plays each part in turn, so a
     * sound can be attached per part via {@link #reloadPartSound(String)}.
     * <p>
     * The default is a single part covering the whole reload animation, which keeps
     * non-split guns behaving exactly as before.
     */
    protected List<ReloadPart> reloadParts(ItemStack stack) {
        return List.of(new ReloadPart(reloadAnimation(stack), reloadDurationTicks(stack)));
    }

    /**
     * Sound played when a reload part begins. Defaults to silent so existing audio is
     * unchanged; override per part name to give each reload step its own sound.
     */
    protected SoundEvent reloadPartSound(String part) {
        return null;
    }

    protected SoundEvent reloadPartEndSound(String part) {
        return null;
    }

    protected SoundEvent reloadCompleteSound() {
        return reloadSound();
    }

    /**
     * Starts a reload part: marks its animation and plays its per-part sound (if any).
     * Called by the reload queue as each part begins.
     */
    public void playReloadPart(ItemStack stack, Level world, LivingEntity user, ReloadPart part) {
        markAnimation(stack, part.animation());
        SoundEvent sound = reloadPartSound(part.animation());
        if (sound != null && !world.isClientSide) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundSource.PLAYERS, 1.4f, 1.0f);
        }
    }

    public void playReloadPartEnd(ItemStack stack, Level world, LivingEntity user, ReloadPart part) {
        SoundEvent sound = reloadPartEndSound(part.animation());
        if (sound != null && !world.isClientSide) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundSource.PLAYERS, 1.4f, 1.0f);
        }
    }

    private boolean hasReloadPartSounds(List<ReloadPart> parts) {
        for (ReloadPart part : parts) {
            if (reloadPartSound(part.animation()) != null || reloadPartEndSound(part.animation()) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isAutomatic() {
        return false;
    }

    protected void markAnimation(ItemStack stack, String animation) {
        if (animation == null || animation.isEmpty()) {
            return;
        }
        CompoundTag data = stack.getOrCreateTag();
        data.putString(ANIMATION_ID, animation);
        data.putLong(ANIMATION_SEQUENCE_ID, data.getLong(ANIMATION_SEQUENCE_ID) + 1);
    }

    protected boolean canFire(Player player, ItemStack stack) {
        if (isOnCooldown(player, stack)) {
            return false;
        }
        if (isReloading(stack)) {
            return false;
        }
        return getShots(stack) >= 1 || player.isCreative();
    }

    private void playEmptyFireSound(Level world, Player user, ItemStack stack) {
        if (!world.isClientSide && !user.isCreative() && !isReloading(stack) && getShots(stack) < 1) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), MilicraftSoundRegistry.WEAP_TRIGGER_HAMMER.get(), SoundSource.PLAYERS, 0.7f, 1.0f);
        }
    }

    /**
     * Default tracer colour as a packed RGB int (alpha ignored). Override per gun to
     * change the tracer colour for that weapon's ammo type.
     */
    protected int tracerColor() {
        return 0xFFE08A; // yellow
    }

    // Spread scales with how much the shooter is moving; aiming (GunAiming) stacks on top.
    private static final float STANDING_SPREAD_MULT = 1.5f;
    private static final float WALKING_SPREAD_MULT = 2.0f;
    private static final float SPRINTING_SPREAD_MULT = 2.5f;

    private static float movementSpreadMultiplier(LivingEntity user) {
        if (user.isSprinting()) {
            return SPRINTING_SPREAD_MULT;
        }
        double dx = user.getX() - user.xOld;
        double dz = user.getZ() - user.zOld;
        if (dx * dx + dz * dz > 1.0e-4) {
            return WALKING_SPREAD_MULT;
        }
        return STANDING_SPREAD_MULT;
    }

    public void fire(ItemStack itemStack, Level world, LivingEntity user) {
        fire(itemStack, world, user, null);
    }

    public void fire(ItemStack itemStack, Level world, LivingEntity user, @Nullable Vec3 muzzle) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        if (!(user instanceof Player player && player.isCreative())) {
            if (shots < 1) {
                return;
            }
            data.putInt(SHOTS_ID, shots - 1);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), fireSound(), SoundSource.PLAYERS, 1f, 1f);

        BulletProjectile bullet = new BulletProjectile(world, user, caliber(), bulletLength(), stunTicks(), 0);
        bullet.setNoGravity(true);
        bullet.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, 12, 0f);
        bullet.setTracerColor(tracerColor());

        // Spawn at the gun's effects bone if the client sent a plausible position,
        // otherwise approximate a barrel position beside the head.
        Vec3 eye = user.getEyePosition();
        Vec3 spawn;
        if (muzzle != null && muzzle.distanceToSqr(eye) < 6.25) {
            spawn = muzzle;
        } else {
            Vec3 right = Vec3.directionFromRotation(0.0f, user.getYRot() + 90.0f);
            spawn = eye.add(user.getLookAngle().scale(0.7)).add(right.scale(0.3)).add(0.0, -0.25, 0.0);
        }
        bullet.setPos(spawn.x, spawn.y, spawn.z);
        bullet.xo = spawn.x;
        bullet.yo = spawn.y;
        bullet.zo = spawn.z;
        bullet.setTracerPath(spawn, bullet.getDeltaMovement());

        float spread = spread() * movementSpreadMultiplier(user);
        if (GunAiming.isAiming(user)) {
            spread *= GunAiming.SPREAD_MULTIPLIER;
        }
        HitscanGunShot.fire(user, damage(), range(), knockback(), barrels(), pelletsPerBarrel(), spread, blockDamage(), bullet);

        world.addFreshEntity(bullet);

        if (user instanceof Player player) {
            addCooldown(player, itemStack, refireCooldownTicks());
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    public void finishReload(ItemStack itemStack, Level world, LivingEntity user) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        if (shots >= maxRounds()) {
            data.putBoolean(RELOADING_ID, false);
            return;
        }

        if (user instanceof Player player) {
            int roundsNeeded = maxRounds() - shots;
            int roundsLoaded = player.isCreative() ? roundsNeeded : consumeAmmoFromInventory(player, roundsNeeded);
            if (roundsLoaded > 0) {
                data.putInt(SHOTS_ID, shots + roundsLoaded);
                SoundEvent completeSound = reloadCompleteSound();
                if (completeSound != null) {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), completeSound, SoundSource.PLAYERS, 1.4f, 1.0f);
                }
            }
            data.putBoolean(RELOADING_ID, false);
            clearCooldown(itemStack);
        }
    }

    protected void addCooldown(Player player, ItemStack stack, int ticks) {
        if (ticks <= 0) {
            return;
        }
        CompoundTag data = stack.getOrCreateTag();
        long cooldownUntil = player.level().getGameTime() + ticks;
        data.putLong(COOLDOWN_UNTIL_ID, Math.max(data.getLong(COOLDOWN_UNTIL_ID), cooldownUntil));
    }

    protected boolean isOnCooldown(Player player, ItemStack stack) {
        return cooldownTicksRemaining(player, stack) > 0;
    }

    public static boolean isCoolingDown(Player player, ItemStack stack) {
        return cooldownTicksRemaining(player, stack) > 0;
    }

    public static int cooldownTicksRemaining(Player player, ItemStack stack) {
        long cooldownUntil = stack.getOrCreateTag().getLong(COOLDOWN_UNTIL_ID);
        return Math.max(0, (int) (cooldownUntil - player.level().getGameTime()));
    }

    protected void clearCooldown(ItemStack stack) {
        stack.getOrCreateTag().remove(COOLDOWN_UNTIL_ID);
    }

    private static InteractionHand heldHand(Player player, ItemStack stack) {
        return player.getOffhandItem() == stack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    protected boolean hasAmmoInInventory(Player player) {
        Item ammo = ammoItem();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ammo) {
                return true;
            }
            if (stack.getItem() instanceof AmmoBoxItem box
                    && box.getAmmoType() == ammoType()
                    && box.countRounds(stack) > 0) {
                return true;
            }
        }
        return false;
    }

    protected int consumeAmmoFromInventory(Player player, int amount) {
        Item ammo = ammoItem();
        Inventory inventory = player.getInventory();
        int consumed = 0;

        // Loose rounds first.
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == ammo) {
                int toConsume = Math.min(amount - consumed, stack.getCount());
                stack.shrink(toConsume);
                consumed += toConsume;
                if (consumed >= amount) {
                    return consumed;
                }
            }
        }

        // Then pull from any matching ammo boxes.
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof AmmoBoxItem box && box.getAmmoType() == ammoType()) {
                consumed += box.consumeRounds(stack, amount - consumed);
                if (consumed >= amount) {
                    return consumed;
                }
            }
        }
        return consumed;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(SHOTS_ID, maxRounds());
        nbt.putBoolean(RELOADING_ID, false);
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getShots(stack) < maxRounds() || isReloading(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * Mth.clamp(getShots(stack) / (float) maxRounds(), 0.0f, 1.0f));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isReloading(stack) ? 0xFFAA00 : 0xE8D27A;
    }

    public int getShots(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(SHOTS_ID)) {
            nbt.putInt(SHOTS_ID, maxRounds());
        }
        return nbt.getInt(SHOTS_ID);
    }

    public static boolean isReloading(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(RELOADING_ID);
    }
}
