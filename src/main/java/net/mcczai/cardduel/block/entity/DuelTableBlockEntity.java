package net.mcczai.cardduel.block.entity;

import net.mcczai.cardduel.init.ModBlockEntities;
import net.mcczai.cardduel.items.inventory.CardBagContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class DuelTableBlockEntity extends BlockEntity {

    private final List<ItemStack> deck = new ArrayList<>();

    public DuelTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DUELTABLE.get(), pos, blockState);
    }

    public List<ItemStack> getDeck() {
        return this.deck;
    }

    public void setDeck(List<ItemStack> cards) {
        this.deck.clear();
        this.deck.addAll(cards.stream().limit(CardBagContents.MAX_SIZE).map(ItemStack::copy).toList());
        sync();
    }

    public void clearDeck() {
        this.deck.clear();
        sync();
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag list = new ListTag();
        for (ItemStack stack : this.deck) {
            list.add(stack.saveOptional(provider));
        }
        tag.put("Deck", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.deck.clear();
        ListTag list = tag.getList("Deck", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(provider, list.getCompound(i));
            if (!stack.isEmpty()) {
                this.deck.add(stack);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.loadAdditional(tag, provider);
        }
    }
}
