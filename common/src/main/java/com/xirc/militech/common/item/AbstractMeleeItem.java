package com.xirc.militech.common.item;

import com.xirc.militech.registry.MilitechPacketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMeleeItem extends Item {
    public static final String ANIMATION_ID = "MilitechMeleeAnimation";
    public static final String ANIMATION_SEQUENCE_ID = "MilitechMeleeAnimationSequence";
    public static final String COOLDOWN_UNTIL_ID = "MilitechMeleeCooldownUntil";

    protected AbstractMeleeItem(Properties settings) {
        super(settings);
    }

    public abstract String playerAnimationFile();

    public void handleInput(ServerPlayer player, ItemStack stack, MilitechPacketRegistry.MeleeInput input) {
        if (player.isSpectator() || isOnCooldown(player, stack)) {
            return;
        }
        switch (input) {
            case PRIMARY -> primaryAttack(player, stack);
            case SECONDARY -> secondaryAttack(player, stack);
            case TERTIARY -> tertiaryAttack(player, stack);
        }
    }

    protected void primaryAttack(ServerPlayer player, ItemStack stack) {
    }

    protected void secondaryAttack(ServerPlayer player, ItemStack stack) {
    }

    protected void tertiaryAttack(ServerPlayer player, ItemStack stack) {
    }

    @Nullable
    protected LivingEntity meleeTarget(ServerPlayer player, double reach, double radius) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(reach));
        AABB search = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(radius + 1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, search,
                entity -> entity instanceof LivingEntity && entity.isPickable() && entity != player,
                radius);
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Nullable
    protected LivingEntity performMeleeHit(ServerPlayer player, ItemStack stack, float damage, double reach, double radius) {
        LivingEntity target = meleeTarget(player, reach, radius);
        if (target == null) {
            return null;
        }
        target.hurt(player.damageSources().playerAttack(player), damage);
        return target;
    }

    protected void markAnimation(ItemStack stack, String animation) {
        if (animation == null || animation.isEmpty()) {
            return;
        }
        CompoundTag data = stack.getOrCreateTag();
        data.putString(ANIMATION_ID, animation);
        data.putLong(ANIMATION_SEQUENCE_ID, data.getLong(ANIMATION_SEQUENCE_ID) + 1);
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

    public static int cooldownTicksRemaining(Player player, ItemStack stack) {
        long cooldownUntil = stack.getOrCreateTag().getLong(COOLDOWN_UNTIL_ID);
        return Math.max(0, (int) (cooldownUntil - player.level().getGameTime()));
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
}
