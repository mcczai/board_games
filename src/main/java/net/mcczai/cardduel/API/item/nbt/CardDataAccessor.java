package net.mcczai.cardduel.API.item.nbt;

import net.mcczai.cardduel.item.ICard;
import net.mcczai.cardduel.resources.DefaultAssets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface CardDataAccessor extends ICard{
    String CARD_NAME = "CardId";
    String CARD_HP = "CardHp";
    String CARD_MP = "CardMp";
    String CARD_ATK = "CardAtk";
    String CARD_TYPE = "CardType";
    String CARD_SKILL = "CardSkill";

    default ResourceLocation getCardId(@NotNull ItemStack card){
        CompoundTag nbt = card.getOrCreateTag();
        if (nbt.contains(CARD_NAME, Tag.TAG_STRING)) {
            ResourceLocation cardId = ResourceLocation.tryParse(nbt.getString(CARD_NAME));
            return Objects.requireNonNullElse(cardId, DefaultAssets.EMPTY_CARD_ID);
        }
        return DefaultAssets.EMPTY_CARD_ID;
    }

    default void setCardId(ItemStack card,ResourceLocation cardId){
        CompoundTag nbt = card.getOrCreateTag();
        if (cardId != null){
            nbt.putString(CARD_NAME,cardId.toString());
        }
    }

    default int getHP(@NotNull ItemStack card){
        CompoundTag nbt = card.getOrCreateTag();
        return Math.max(1,nbt.getInt(CARD_HP));
    }

    default void setHP(@NotNull ItemStack card, int amount){
        CompoundTag nbt = card.getOrCreateTag();
        nbt.putInt(CARD_HP,Math.max(amount,1));
    }

    @Override
    default int getMP(@NotNull ItemStack card){
        CompoundTag nbt = card.getOrCreateTag();
        return Math.max(1,nbt.getInt(CARD_MP));
    }

    default void setMP(@NotNull ItemStack card, int amount){
        CompoundTag nbt = card.getOrCreateTag();
        nbt.putInt(CARD_MP,Math.max(amount,1));
    }

    @Override
    default int getATK(@NotNull ItemStack card){
        CompoundTag nbt = card.getOrCreateTag();
        return Math.max(1,nbt.getInt(CARD_ATK));
    }

    default void setATK(@NotNull ItemStack card, int amount){
        CompoundTag nbt = card.getOrCreateTag();
        nbt.putInt(CARD_ATK,Math.max(amount,1));
    }

    @Override
    default String getType(@NotNull ItemStack card){
        CompoundTag nbt = card.getOrCreateTag();
        if (nbt.get(CARD_TYPE) != null){
            return nbt.getString(CARD_TYPE);
        }
        return DefaultAssets.DEFAULT_TYPE.toString();
    }

    default void setType(@NotNull ItemStack card, String type){
        CompoundTag nbt = card.getOrCreateTag();
        nbt.putString(CARD_TYPE,type);
    }

    @Override
    default String getSkill(@NotNull ItemStack card){
        CompoundTag nbt = card.getOrCreateTag();
        if (nbt.get(CARD_SKILL) != null){
            return nbt.getString(CARD_SKILL);
        }
        return DefaultAssets.DEFAULT_SKILL.toString();
    }

    @Override
    default void setSkill(@NotNull ItemStack card, String skill){
        CompoundTag nbt = card.getOrCreateTag();
        nbt.putString(CARD_SKILL,skill);
    }
}
