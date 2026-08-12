package net.mcczai.cardduel.items.inventory;

import net.mcczai.cardduel.init.ModDataComponents;
import net.mcczai.cardduel.init.ModMenuType;
import net.mcczai.cardduel.items.ICard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CardBagMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = CardBagContents.MAX_SIZE;

    private final ItemStack bagStack;

    public CardBagMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    public CardBagMenu(int containerId, Inventory playerInventory, ItemStack bagStack) {
        super(ModMenuType.CARD_BAG_MENU.get(), containerId);
        this.bagStack = bagStack;

        for (int i = 0; i < CONTAINER_SIZE; i++) {
            this.addSlot(new CardBagSlot(this, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));
        }

        for (int l = 0; l < 3; l++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 96));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 154));
        }
    }

    public CardBagContents getContents() {
        return this.bagStack.getOrDefault(ModDataComponents.CARD_BAG, CardBagContents.EMPTY);
    }

    public void setContents(CardBagContents contents) {
        this.bagStack.set(ModDataComponents.CARD_BAG, contents);
    }

    public boolean canInsertCard(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }
        if (!(stack.getItem() instanceof ICard card)) {
            return false;
        }
        if (stack.getOrDefault(ModDataComponents.IN_DUEL, false)) {
            return false;
        }
        CardBagContents contents = this.getContents();
        if (contents.size() >= CONTAINER_SIZE) {
            return false;
        }
        ResourceLocation cardId = card.getCardId(stack);
        return !contents.containsCard(cardId);
    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < CONTAINER_SIZE) {
                if (!this.moveItemStackTo(itemstack1, CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, CONTAINER_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public static class CardBagSlot extends Slot {
        private final CardBagMenu menu;
        private final int index;

        public CardBagSlot(CardBagMenu menu, int index, int x, int y) {
            super(new SimpleContainer(1), index, x, y);
            this.menu = menu;
            this.index = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.menu.canInsertCard(stack);
        }

        @Override
        public ItemStack getItem() {
            return this.menu.getContents().getItem(this.index);
        }

        @Override
        public void set(ItemStack stack) {
            CardBagContents.Mutable mutable = new CardBagContents.Mutable(this.menu.getContents());
            mutable.setItem(this.index, stack);
            this.menu.setContents(mutable.toImmutable());
            this.setChanged();
        }

        @Override
        public ItemStack remove(int amount) {
            CardBagContents.Mutable mutable = new CardBagContents.Mutable(this.menu.getContents());
            ItemStack removed = mutable.removeItem(this.index, amount);
            this.menu.setContents(mutable.toImmutable());
            return removed;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
