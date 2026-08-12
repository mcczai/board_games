package net.mcczai.cardduel.items;

import net.mcczai.cardduel.API.CdAPI;
import net.mcczai.cardduel.API.item.CardTabType;
import net.mcczai.cardduel.items.builder.CardItemBuilder;
import net.mcczai.cardduel.resources.CommonCardIndex;
import net.mcczai.cardduel.resources.pojo.CardDataPOJO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCardItem extends Item implements ICard {
    public AbstractCardItem(Properties properties) {
        super(properties);
    }

    /**
     * 右键时触发的事件
     */
    public abstract void rClick(ItemStack cardItem, LivingEntity Player, BlockPos blockPos);


    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        ResourceLocation cardId = this.getCardId(stack);
        Optional<CommonCardIndex> cardIndex = CdAPI.getCommonCardIndex(cardId);
        if (cardIndex.isPresent()){
            String name = cardIndex.get().getPojo().getName();
            if (StringUtils.isBlank(name)){
                name = "custom.cardduel.err.no_name";
            }
            return Component.translatable(name);
        }
        return super.getName(stack);
    }

    /**
     *根据卡牌类型进行分类注册(是否需要？)
     */
    public static @NotNull NonNullList<ItemStack> fillItemTab(CardTabType type){
        NonNullList<ItemStack> stacks = NonNullList.create();
        String key = type.name().toLowerCase(Locale.US);
        CdAPI.getAllCommonCardIndex().stream().sorted().forEach(entry ->{
            CommonCardIndex index = entry.getValue();
            CardDataPOJO cardDataPOJO = index.getCardData();
            if (!key.equals(cardDataPOJO.getTYPE())) {
                return;
            }
            ItemStack itemStack = CardItemBuilder.create()
                    .setId(entry.getKey())
                    .setHP(cardDataPOJO.getHP())
                    .setMP(cardDataPOJO.getMP())
                    .setATK(cardDataPOJO.getATK())
                    .setSkill(cardDataPOJO.getSKILL())
                    .setType(cardDataPOJO.getTYPE())
                    .setTribe(cardDataPOJO.getTRIBE())
                    .build();
            stacks.add(itemStack);
        });
        return stacks;
    }
}
