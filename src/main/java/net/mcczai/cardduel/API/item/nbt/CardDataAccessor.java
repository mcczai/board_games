package net.mcczai.cardduel.API.item.nbt;

import net.mcczai.cardduel.init.ModDataComponents;
import net.mcczai.cardduel.items.ICard;
import net.mcczai.cardduel.items.component.CardDataComponent;
import net.mcczai.cardduel.resources.DefaultAssets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface CardDataAccessor extends ICard {

    default CardDataComponent getOrCreateCardData(@NotNull ItemStack card) {
        return card.getOrDefault(ModDataComponents.CARD_DATA, CardDataComponent.DEFAULT);
    }

    default ResourceLocation getCardId(@NotNull ItemStack card) {
        ResourceLocation id = card.get(ModDataComponents.CARD_ID);
        return id != null ? id : DefaultAssets.EMPTY_CARD_ID;
    }

    default void setCardId(ItemStack card, ResourceLocation cardId) {
        card.set(ModDataComponents.CARD_ID, cardId != null ? cardId : DefaultAssets.EMPTY_CARD_ID);
    }

    default int getHP(@NotNull ItemStack card) {
        CardDataComponent data = card.get(ModDataComponents.CARD_DATA);
        return data != null ? data.hp() : 1;
    }

    default void setHP(@NotNull ItemStack card, int amount) {
        CardDataComponent data = getOrCreateCardData(card);
        card.set(ModDataComponents.CARD_DATA, data.withHp(amount));
    }

    @Override
    default int getMP(@NotNull ItemStack card) {
        CardDataComponent data = card.get(ModDataComponents.CARD_DATA);
        return data != null ? data.mp() : 1;
    }

    default void setMP(@NotNull ItemStack card, int amount) {
        CardDataComponent data = getOrCreateCardData(card);
        card.set(ModDataComponents.CARD_DATA, data.withMp(amount));
    }

    @Override
    default int getATK(@NotNull ItemStack card) {
        CardDataComponent data = card.get(ModDataComponents.CARD_DATA);
        return data != null ? data.atk() : 1;
    }

    default void setATK(@NotNull ItemStack card, int amount) {
        CardDataComponent data = getOrCreateCardData(card);
        card.set(ModDataComponents.CARD_DATA, data.withAtk(amount));
    }

    @Override
    default String getType(@NotNull ItemStack card) {
        CardDataComponent data = card.get(ModDataComponents.CARD_DATA);
        return data != null ? data.type() : "trap";
    }

    default void setType(@NotNull ItemStack card, String type) {
        CardDataComponent data = getOrCreateCardData(card);
        card.set(ModDataComponents.CARD_DATA, data.withType(type));
    }

    @Override
    default String getSkill(@NotNull ItemStack card) {
        CardDataComponent data = card.get(ModDataComponents.CARD_DATA);
        return data != null ? data.skill() : "0";
    }

    @Override
    default void setSkill(@NotNull ItemStack card, String skill) {
        CardDataComponent data = getOrCreateCardData(card);
        card.set(ModDataComponents.CARD_DATA, data.withSkill(skill));
    }

    @Override
    @Nullable
    default String getTribe(@NotNull ItemStack card) {
        CardDataComponent data = card.get(ModDataComponents.CARD_DATA);
        return data != null ? data.tribe() : null;
    }

    @Override
    default void setTribe(@NotNull ItemStack card, String tribe) {
        CardDataComponent data = getOrCreateCardData(card);
        card.set(ModDataComponents.CARD_DATA, data.withTribe(tribe));
    }
}
