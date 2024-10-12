package net.mcczai.cardduel.items;

import net.mcczai.cardduel.API.CdAPI;
import net.mcczai.cardduel.API.item.CardTabType;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.client.resource.ClientCardIndex;
import net.mcczai.cardduel.items.builder.CardItemBuilder;
import net.mcczai.cardduel.resources.CommonCardIndex;
import net.mcczai.cardduel.resources.pojo.CardDataPOJO;
import net.mcczai.cardduel.resources.pojo.CardIndexPOJO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    public abstract void Rclick(ItemStack cardItem, LivingEntity Player, BlockPos blockPos);

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        ResourceLocation cardId = this.getCardId(stack);
        Optional<ClientCardIndex> cardIndex = CdAPI.getClientCardIndex(cardId);
        if (cardIndex.isPresent()){
            return Component.translatable(cardIndex.get().getName());
        }
        return super.getName(stack);
    }

    /**
     *根据卡牌类型进行分类注册(是否需要？)
     */
    public static @NotNull NonNullList<ItemStack> fillItemTab(CardTabType type){
        NonNullList<ItemStack> stacks = NonNullList.create();
        CdAPI.getAllCommonCardIndex().stream().sorted().forEach(entry ->{
            CommonCardIndex index = entry.getValue();
            CardDataPOJO cardDataPOJO = index.getCardData();
            CardIndexPOJO cardIndexPOJO = index.getPojo();
            String key = type.name().toLowerCase(Locale.US);

            switch (key){
                case "trap" :
                    ItemStack itemStack = CardItemBuilder.create()
                            //TODO: 这里是否要改成这样？ 用于设置卡牌名称
                            //TODO: .setId(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID , cardIndexPOJO.getName()))
                            .setId(entry.getKey())
                            .setMP(cardDataPOJO.getMP())
                            .setSkill(cardDataPOJO.getSKILL())
                            .setType(cardDataPOJO.getTYPE())
                            .build();
                    stacks.add(itemStack);
                case "mana" :
                    itemStack = CardItemBuilder.create()
                            .setId(entry.getKey())
                            .setMP(cardDataPOJO.getMP())
                            .setSkill(cardDataPOJO.getSKILL())
                            .setType(cardDataPOJO.getTYPE())
                            .build();
                    stacks.add(itemStack);
                case "equip" :
                    itemStack = CardItemBuilder.create()
                            .setId(entry.getKey())
                            .setMP(cardDataPOJO.getMP())
                            .setATK(cardDataPOJO.getATK())
                            .setSkill(cardDataPOJO.getSKILL())
                            .setType(cardDataPOJO.getTYPE())
                            .build();
                    stacks.add(itemStack);
                case "summon" :
                    itemStack = CardItemBuilder.create()
                            .setId(entry.getKey())
                            .setATK(cardDataPOJO.getATK())
                            .setHP(cardDataPOJO.getHP())
                            .setMP(cardDataPOJO.getMP())
                            .setSkill(cardDataPOJO.getSKILL())
                            .setType(cardDataPOJO.getTYPE())
                            .build();
                    stacks.add(itemStack);
                default:
                    itemStack = CardItemBuilder.create()
                            .setId(entry.getKey())
                            .setATK(cardDataPOJO.getATK())
                            .setHP(cardDataPOJO.getHP())
                            .setMP(cardDataPOJO.getMP())
                            .setSkill(cardDataPOJO.getSKILL())
                            .setType(cardDataPOJO.getTYPE())
                            .build();
                    stacks.add(itemStack);
            }
        });
        return stacks;
    }
}
