package net.mcczai.cardduel.items.builder;

import com.google.common.base.Preconditions;
import net.mcczai.cardduel.API.CdAPI;
import net.mcczai.cardduel.init.ModItem;
import net.mcczai.cardduel.items.ICard;
import net.mcczai.cardduel.resources.CommonCardIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CardItemBuilder {
    private int count = 1;
    private int hp = 1;
    private int mp = 1;
    private int atk = 1;
    private String type = null;
    private ResourceLocation cardId;
    private String skill;
    private String tribe;

    private CardItemBuilder(){

    }

    public static CardItemBuilder create(){
        return new CardItemBuilder();
    }

    public CardItemBuilder setHP(int hp){
        this.hp = Math.max(hp,1);
        return this;
    }

    public CardItemBuilder setMP(int mp){
        this.mp = Math.max(mp,1);
        return this;
    }

    public CardItemBuilder setATK(int atk){
        this.atk = Math.max(atk,1);
        return this;
    }

    public CardItemBuilder setType(String type){
        this.type = type;
        return this;
    }

    public CardItemBuilder setId(ResourceLocation id) {
        this.cardId = id;
        return this;
    }

    public CardItemBuilder setSkill(String skill){
        this.skill = skill;
        return this;
    }

    public CardItemBuilder setTribe(String tribe){
        this.tribe = tribe;
        return this;
    }

    public @NotNull ItemStack build(){
        String type = CdAPI.getCommonCardIndex(cardId).map(CommonCardIndex::getType).orElse(null);
        Preconditions.checkArgument(type != null, "Could not found card id: " + cardId);

        ItemStack card = new ItemStack(ModItem.CARD_ITEM.get(),this.count);
        if (card.getItem() instanceof ICard iCard){
            iCard.setCardId(card,this.cardId);
            iCard.setHP(card,this.hp);
            iCard.setMP(card,this.mp);
            iCard.setATK(card,this.atk);
            iCard.setType(card,this.type);
            iCard.setSkill(card,this.skill);
            iCard.setTribe(card,this.tribe);
        }
        return card;
    }
}
