package com.xirc.militech.common.block;

import com.xirc.militech.common.menu.GunBenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Workbench block that opens the gun bench assembly menu. Assembly parts are
 * transient (returned to the player on close, like a crafting table); blueprints
 * persist in the bench's block entity.
 */
public class GunBenchBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final Component TITLE = Component.translatable("block.militech.gun_bench");

    public GunBenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GunBenchBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer
                && world.getBlockEntity(pos) instanceof GunBenchBlockEntity bench) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (syncId, inventory, p) -> new GunBenchMenu(syncId, inventory,
                            ContainerLevelAccess.create(world, pos), bench.getBlueprints()),
                    TITLE));
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof GunBenchBlockEntity bench) {
            Containers.dropContents(world, pos, bench.getBlueprints());
        }
        super.onRemove(state, world, pos, newState, moved);
    }
}
