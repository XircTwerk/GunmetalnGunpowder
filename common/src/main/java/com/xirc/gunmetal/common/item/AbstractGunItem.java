package com.xirc.gunmetal.common.item;

import com.xirc.gunmetal.common.entity.projectile.BulletProjectile;
import com.xirc.gunmetal.common.system.hitscan.HitscanGunShot;
import com.xirc.gunmetal.common.tickable.PlaceholderGunReload;
import com.xirc.gunmetal.common.util.DimensionData;
import com.xirc.gunmetal.registry.GunmetalItems;
import com.xirc.gunmetal.registry.GunmetalSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractGunItem extends Item {
    public static final String SHOTS_ID = "Shots";
    public static final String RELOADING_ID = "Reloading";
    public static final String ANIMATION_ID = "GunmetalGunAnimation";
    public static final String ANIMATION_SEQUENCE_ID = "GunmetalGunAnimationSequence";

    protected AbstractGunItem(Properties settings) {
        super(settings);
    }

    /**
     * Maximum ammo stored in this gun's item NBT.
     * <p>
     * This controls the ammo tooltip, durability-style ammo bar, default loaded ammo,
     * and when the reload queue stops loading more rounds.
     */
    protected abstract int maxRounds();

    /**
     * Base damage dealt by each hitscan shot before range falloff is applied.
     * <p>
     * The visual bullet entity is currently spawned with zero damage, so this value is
     * applied by {@link HitscanGunShot} instead of by the projectile.
     */
    protected abstract float damage();

    /**
     * Maximum hitscan distance in blocks.
     * <p>
     * Shots trace from the shooter's eye position up to this range, stopping early if
     * a solid block clips the ray. Damage also falls from normal damage at 8 blocks
     * to zero damage at this distance.
     */
    protected abstract float range();

    /**
     * Knockback strength applied to living targets hit by the hitscan shot.
     * <p>
     * Higher values push targets farther away from the shooter.
     */
    protected abstract float knockback();

    /**
     * Number of hitscan rays fired per shot.
     * <p>
     * Use {@code 1} for a single bullet. Higher values act like multiple pellets and
     * can hit multiple targets along slightly randomized rays.
     */
    protected abstract int barrels();

    /**
     * Bullet diameter used by the visual projectile, measured in millimeters.
     * <p>
     * This feeds the projectile mass calculation for penetration and ricochet behavior.
     */
    protected abstract float caliber();

    /**
     * Bullet length used by the visual projectile, measured in millimeters.
     * <p>
     * Together with {@link #caliber()}, this feeds the projectile mass calculation.
     */
    protected abstract float bulletLength();

    /**
     * Stun duration carried by the visual bullet projectile, measured in ticks.
     * <p>
     * The current plain Minecraft implementation stores this value for future combat
     * effects, but does not yet apply a stun effect on hit.
     */
    protected abstract int stunTicks();

    /**
     * Short cooldown added immediately when the player starts a fire input.
     * <p>
     * This prevents duplicate input handling before the main refire cooldown is applied.
     */
    protected abstract int inputCooldownTicks();

    /**
     * Main cooldown after a successful shot, measured in ticks.
     * <p>
     * While this cooldown is active, the gun cannot fire again. Minecraft runs at 20
     * ticks per second under normal conditions.
     */
    protected abstract int refireCooldownTicks();

    /**
     * Cooldown shown while reload input is active, measured in ticks.
     * <p>
     * This is applied when reload begins and again after each loaded round if more
     * rounds can still be loaded.
     */
    protected abstract int reloadCooldownTicks();

    /**
     * Delay between individual rounds being loaded, measured in ticks.
     * <p>
     * The reload queue waits this long before calling {@link #finishReload(ItemStack, Level, LivingEntity)}.
     */
    protected abstract int reloadStepTicks();

    /**
     * Sound played when this gun fires.
     */
    protected SoundEvent fireSound() {
        return GunmetalSoundEvents.REVOLVER_FIRE.get();
    }

    /**
     * Sound played when reload starts and when each round is loaded.
     */
    protected SoundEvent reloadSound() {
        return GunmetalSoundEvents.LOAD.get();
    }

    /**
     * Item consumed for each loaded round unless the player is in creative mode.
     */
    protected Item ammoItem() {
        return GunmetalItems.BULLET.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        int shots = getShots(stack);
        tooltip.add(Component.translatable("tooltip.gunmetal.gun.ammo")
                .append(Component.literal(" " + shots + "/" + maxRounds()).withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.translatable("tooltip.gunmetal.gun.fire").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gunmetal.gun.reload").withStyle(ChatFormatting.GRAY));
        if (isReloading(stack)) {
            tooltip.add(Component.translatable("tooltip.gunmetal.gun.reloading").withStyle(ChatFormatting.GOLD));
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
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.isSpectator()) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!user.isShiftKeyDown()) {
            return fireFromUse(world, user, itemStack);
        }

        return reloadFromUse(world, user, itemStack);
    }

    private InteractionResultHolder<ItemStack> fireFromUse(Level world, Player user, ItemStack itemStack) {
        if (!canFire(user, itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        markAnimation(itemStack, fireAnimation());
        if (!world.isClientSide) {
            user.getCooldowns().addCooldown(this, inputCooldownTicks());
            fire(itemStack, world, user);
        }
        return InteractionResultHolder.success(itemStack);
    }

    private InteractionResultHolder<ItemStack> reloadFromUse(Level world, Player user, ItemStack itemStack) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        if (shots >= maxRounds()) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (data.getBoolean(RELOADING_ID) && user.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!user.isCreative() && !hasAmmoInInventory(user)) {
            return InteractionResultHolder.fail(itemStack);
        }

        markAnimation(itemStack, reloadAnimation());
        if (!world.isClientSide) {
            data.putBoolean(RELOADING_ID, true);
            user.getCooldowns().addCooldown(this, reloadCooldownTicks());
            PlaceholderGunReload.enqueue(new DimensionData(user, world.dimension(), reloadStepTicks()));
            world.playSound(null, user.getX(), user.getY(), user.getZ(), reloadSound(), SoundSource.PLAYERS, 0.5f, 1.0f);
        }

        return InteractionResultHolder.success(itemStack);
    }

    public static boolean handleLeftClick(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack gunStack = null;
        AbstractGunItem gun = null;
        if (mainHand.getItem() instanceof AbstractGunItem mainGun) {
            gunStack = mainHand;
            gun = mainGun;
        } else if (offHand.getItem() instanceof AbstractGunItem offGun) {
            gunStack = offHand;
            gun = offGun;
        }

        if (gun == null) {
            return false;
        }

        Level world = player.level();

        if (!gun.canFire(player, gunStack)) {
            return true;
        }

        if (!world.isClientSide) {
            player.getCooldowns().addCooldown(gun, gun.inputCooldownTicks());
            gun.fire(gunStack, world, player);
        }

        return true;
    }

    protected String fireAnimation() {
        return "fire";
    }

    protected String reloadAnimation() {
        return "reload";
    }

    private void markAnimation(ItemStack stack, String animation) {
        if (animation == null || animation.isEmpty()) {
            return;
        }
        CompoundTag data = stack.getOrCreateTag();
        data.putString(ANIMATION_ID, animation);
        data.putLong(ANIMATION_SEQUENCE_ID, data.getLong(ANIMATION_SEQUENCE_ID) + 1);
    }

    protected boolean canFire(Player player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }
        if (isReloading(stack)) {
            return false;
        }
        return getShots(stack) >= 1 || player.isCreative();
    }

    public void fire(ItemStack itemStack, Level world, LivingEntity user) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        if (!(user instanceof Player player && player.isCreative())) {
            if (shots < 1) {
                return;
            }
            data.putInt(SHOTS_ID, shots - 1);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), fireSound(), SoundSource.PLAYERS, 1f, 1f);

        HitscanGunShot.fire(user, damage(), range(), knockback(), barrels());

        BulletProjectile bullet = new BulletProjectile(world, user, caliber(), bulletLength(), stunTicks(), 0);
        bullet.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, 10, 0f);

        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    bullet.getX(), bullet.getY(), bullet.getZ(),
                    5,
                    0.1, 0.1, 0.1,
                    0.02);
        }

        world.addFreshEntity(bullet);

        if (user instanceof Player player) {
            player.getCooldowns().addCooldown(this, refireCooldownTicks());
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
            if (consumeAmmoFromInventory(player)) {
                data.putInt(SHOTS_ID, shots + 1);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), reloadSound(), SoundSource.PLAYERS, 0.7f, 1.0f);

                if (data.getInt(SHOTS_ID) < maxRounds() && hasAmmoInInventory(player)) {
                    PlaceholderGunReload.enqueue(new DimensionData(user, world.dimension(), reloadStepTicks()));
                    player.getCooldowns().addCooldown(this, reloadCooldownTicks());
                } else {
                    data.putBoolean(RELOADING_ID, false);
                    player.getCooldowns().removeCooldown(this);
                }
            } else {
                data.putBoolean(RELOADING_ID, false);
                player.getCooldowns().removeCooldown(this);
            }
        }
    }

    protected boolean hasAmmoInInventory(Player player) {
        return player.getInventory().contains(new ItemStack(ammoItem()));
    }

    protected boolean consumeAmmoFromInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ammoItem()) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
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
