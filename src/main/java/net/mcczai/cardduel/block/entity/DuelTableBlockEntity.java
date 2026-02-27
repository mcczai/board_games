package net.mcczai.cardduel.block.entity;

import net.mcczai.cardduel.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DuelTableBlockEntity extends BlockEntity {

    public static final BlockEntityType<DuelTableBlockEntity> TYPE = BlockEntityType.Builder.of(DuelTableBlockEntity::new, ModBlocks.DUELTABLE_BLOCK.get()).build(null);

    public DuelTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(TYPE,pos, blockState);
    }
}
