package net.mcczai.cardduel.API.item.nbt;

import com.google.errorprone.annotations.Var;
import net.mcczai.cardduel.init.ModDataComponents;
import net.mcczai.cardduel.items.ICard;
import net.mcczai.cardduel.resources.DefaultAssets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
// TODO: 这边改用高版本的组件，不要再直接往tag里写东西了
public interface CardDataAccessor extends ICard{
    String CARD_NAME = "CardId";

    default ResourceLocation getCardId(@NotNull ItemStack card){
       Component data = card.get(DataComponents.ITEM_NAME);
       if (data != null){
           ResourceLocation cardId = ResourceLocation.tryParse(CARD_NAME);

           return Objects.requireNonNullElse(cardId, DefaultAssets.EMPTY_CARD_ID);
       }
        return DefaultAssets.EMPTY_CARD_ID;
    }

    default void setCardId(ItemStack card,ResourceLocation cardId){
        if (cardId != null){
            Component dataId = Component.nullToEmpty(cardId.toString());
            card.set(DataComponents.ITEM_NAME,dataId);
            return;
        }
        Component DefaultId = Component.nullToEmpty(DefaultAssets.EMPTY_CARD_ID.toString());
        card.set(DataComponents.ITEM_NAME,DefaultId);
    }

    default int getHP(@NotNull ItemStack card){
        if (card.get(ModDataComponents.HP) != null){
            return card.get(ModDataComponents.HP);
        }
        return 1;
    }

    default void setHP(@NotNull ItemStack card, int amount){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("HP",Math.max(amount,1));
        card.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }

    @Override
    default int getMP(@NotNull ItemStack card){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (tag.get("MP") != null) {
            return tag.getInt("MP");
        }
        return 1;
    }

    default void setMP(@NotNull ItemStack card, int amount){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("MP",Math.max(amount,1));
        card.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }

    @Override
    default int getATK(@NotNull ItemStack card){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (tag.get("ATK") != null) {
            return tag.getInt("ATK");
        }
        return 1;
    }

    default void setATK(@NotNull ItemStack card, int amount){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("ATK",Math.max(amount,1));
        card.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }

    @Override
    default int getType(@NotNull ItemStack card){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (tag.get("Type") != null) {
            return tag.getInt("Type");
        }
        return 0;
    }

    default void setType(@NotNull ItemStack card, String type){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("Type",type);
        card.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }

    @Override
    default String getSkill(@NotNull ItemStack card){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.get("Skill") != null) {
            return tag.getString("Skill");
        }
        return "0";
    }

    @Override
    default void setSkill(@NotNull ItemStack card, String skill){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("Skill",skill);
        card.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
}
