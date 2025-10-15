package net.mcczai.cardduel.item;

import net.mcczai.cardduel.API.item.nbt.CardDataAccessor;
import net.mcczai.cardduel.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class CardItem extends AbstractCardItem implements CardDataAccessor {

    public CardItem(@NotNull Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     *右键牌桌时，判断卡是否为已加入战斗的卡
     */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        Block clickedBlock = level.getBlockState(context.getClickedPos()).getBlock();
        CompoundTag nbt = context.getItemInHand().getOrCreateTag();
        boolean joined = nbt.getBoolean("IsJoinedDuel");

        if (clickedBlock == ModBlocks.DUELTABLE_BLOCK.get() && joined){

        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void Rclick(ItemStack cardItem, LivingEntity Player, BlockPos blockPos) {

    }

}

