package net.mcczai.cardduel.items.inventory;

import com.mojang.serialization.Codec;
import net.mcczai.cardduel.items.ICard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CardBagContents {
    public static final int MAX_SIZE = 27;

    public static final Codec<CardBagContents> CODEC = ItemStack.CODEC.listOf().xmap(CardBagContents::new, contents -> contents.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, CardBagContents> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(CardBagContents::new, contents -> contents.items);

    public static final CardBagContents EMPTY = new CardBagContents(List.of());

    private final List<ItemStack> items;

    private CardBagContents(List<ItemStack> items) {
        this.items = items.stream()
                .filter(stack -> !stack.isEmpty())
                .limit(MAX_SIZE)
                .toList();
    }

    public List<ItemStack> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public ItemStack getItemUnsafe(int index) {
        return items.get(index);
    }

    public ItemStack getItem(int index) {
        return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
    }

    public boolean containsCard(ResourceLocation cardId) {
        for (ItemStack item : items) {
            if (item.getItem() instanceof ICard card && cardId.equals(card.getCardId(item))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CardBagContents contents)) {
            return false;
        }
        return ItemStack.listMatches(this.items, contents.items);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public static class Mutable {
        private final List<ItemStack> items;

        public Mutable() {
            this.items = new ArrayList<>();
        }

        public Mutable(CardBagContents contents) {
            this.items = new ArrayList<>(contents.items);
        }

        public Mutable clear() {
            this.items.clear();
            return this;
        }

        public int size() {
            return this.items.size();
        }

        public boolean isFull() {
            return this.items.size() >= MAX_SIZE;
        }

        public ItemStack getItem(int index) {
            return index >= 0 && index < this.items.size() ? this.items.get(index) : ItemStack.EMPTY;
        }

        public boolean containsCard(ResourceLocation cardId) {
            for (ItemStack item : this.items) {
                if (item.getItem() instanceof ICard card && cardId.equals(card.getCardId(item))) {
                    return true;
                }
            }
            return false;
        }

        public void setItem(int index, ItemStack stack) {
            ItemStack copy = stack.copy();
            if (copy.isEmpty()) {
                if (index >= 0 && index < this.items.size()) {
                    this.items.remove(index);
                }
                return;
            }
            copy.setCount(1);
            if (index < this.items.size()) {
                this.items.set(index, copy);
            } else {
                this.items.add(copy);
            }
        }

        public ItemStack removeItem(int index, int amount) {
            if (index < 0 || index >= this.items.size() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = this.items.get(index);
            ItemStack removed = stack.split(amount);
            if (stack.isEmpty()) {
                this.items.remove(index);
            }
            return removed;
        }

        public CardBagContents toImmutable() {
            return new CardBagContents(this.items);
        }
    }
}
