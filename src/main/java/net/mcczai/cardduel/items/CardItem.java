package net.mcczai.cardduel.items;

import net.mcczai.cardduel.client.renderer.CardItemRenderer;
import net.mcczai.cardduel.init.ModBlocks;
import net.mcczai.cardduel.API.item.nbt.CardDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CardItem extends AbstractCardItem implements CardDataAccessor {

    public CardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void rClick(ItemStack cardItem, LivingEntity Player, BlockPos blockPos) {

    }

    /**
     *右键牌桌时，判断卡是否为已加入战斗的卡
     */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        CompoundTag tag = context.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean joined = tag.getBoolean("IsJoinedDuel");

        if (level.getBlockState(context.getClickedPos()).is(ModBlocks.DUELTABLE_BLOCK.get()) && joined){

        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CardItemRenderer.getInstance();
            }
        });
    }

}

