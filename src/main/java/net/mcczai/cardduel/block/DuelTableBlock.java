package net.mcczai.cardduel.block;

import com.mojang.serialization.MapCodec;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.duel.DuelEngine;
import net.mcczai.cardduel.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public class DuelTableBlock extends BaseEntityBlock {

    public static final MapCodec<DuelTableBlock> CODEC = simpleCodec(DuelTableBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

    public DuelTableBlock(@NotNull Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(
            this.stateDefinition
                    .any()
                    .setValue(FACING,Direction.NORTH)
                    .setValue(DOUBLE, false)
        );
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockGetter blockGetter = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState northState = blockGetter.getBlockState(blockPos.relative(Direction.NORTH));
        BlockState southState = blockGetter.getBlockState(blockPos.relative(Direction.SOUTH));
        if (northState.is(ModBlocks.DUELTABLE_BLOCK.get())) {
            return this.defaultBlockState()
                    .setValue(FACING, Direction.NORTH)
                    .setValue(DOUBLE, true);
        }
        if (southState.is(ModBlocks.DUELTABLE_BLOCK.get())) {
            return this.defaultBlockState()
                    .setValue(FACING, Direction.SOUTH)
                    .setValue(DOUBLE, true);
        }
        return this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(DOUBLE, false);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction,
                                              @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        BlockState northState = level.getBlockState(pos.relative(Direction.NORTH));
        BlockState southState = level.getBlockState(pos.relative(Direction.SOUTH));
        boolean northTable = northState.is(ModBlocks.DUELTABLE_BLOCK.get());
        boolean southTable = southState.is(ModBlocks.DUELTABLE_BLOCK.get());
        if (northTable && !southTable) {
            return state.setValue(FACING, Direction.NORTH).setValue(DOUBLE, true);
        }
        if (southTable && !northTable) {
            return state.setValue(FACING, Direction.SOUTH).setValue(DOUBLE, true);
        }
        return state.setValue(FACING, Direction.NORTH).setValue(DOUBLE, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACING,DOUBLE);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof DuelTableBlockEntity table) {
            if (player.isShiftKeyDown()) {
                return DuelEngine.handleLeave(serverPlayer, table);
            }
            return DuelEngine.handleTableUse(serverPlayer, table);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new DuelTableBlockEntity(blockPos,blockState);
    }
}
