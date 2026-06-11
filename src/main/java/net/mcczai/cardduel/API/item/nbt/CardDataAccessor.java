package net.mcczai.cardduel.API.item.nbt;

import net.mcczai.cardduel.init.ModDataComponents;
import net.mcczai.cardduel.items.ICard;
import net.mcczai.cardduel.resources.DefaultAssets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
public interface CardDataAccessor extends ICard{

    default ResourceLocation getCardId(@NotNull ItemStack card){
        ResourceLocation id = card.get(ModDataComponents.CARD_ID);
        return id != null ? id : DefaultAssets.EMPTY_CARD_ID;
    }

    default void setCardId(ItemStack card, ResourceLocation cardId){
        card.set(ModDataComponents.CARD_ID, cardId != null ? cardId : DefaultAssets.EMPTY_CARD_ID);
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
    default String getType(@NotNull ItemStack card){
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (tag.get("Type") != null) {
            return tag.getString("Type");
        }
        return "trap";
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
