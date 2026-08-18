package net.mcczai.cardduel.block.entity;

import net.mcczai.cardduel.duel.DuelPhase;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.mcczai.cardduel.init.ModAttachments;
import net.mcczai.cardduel.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 牌桌方块实体：存储一场对局的全部数据（服务端权威）。
 * 对局逻辑见 duel/DuelEngine，本类只负责数据存取与同步。
 */
public class DuelTableBlockEntity extends BlockEntity {

    public static final int DEFAULT_MANA_CAP = 10;
    public static final int DEFAULT_HP_CAP = 30;

    private DuelPhase phase = DuelPhase.IDLE;
    private int manaCap = DEFAULT_MANA_CAP;
    private int hpCap = DEFAULT_HP_CAP;

    @Nullable
    private UUID hostUuid;
    @Nullable
    private UUID guestUuid;

    private final DuelPlayerData hostData = new DuelPlayerData();
    private final DuelPlayerData guestData = new DuelPlayerData();

    public DuelTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DUELTABLE.get(), pos, blockState);
    }

    // ==================== 阶段与上限 ====================

    public DuelPhase getPhase() {
        return phase;
    }

    public void setPhase(DuelPhase phase) {
        this.phase = phase;
        sync();
    }

    public int getManaCap() {
        return manaCap;
    }

    public int getHpCap() {
        return hpCap;
    }

    public void setCaps(int manaCap, int hpCap) {
        this.manaCap = manaCap;
        this.hpCap = hpCap;
        sync();
    }

    // ==================== 座位 ====================

    @Nullable
    public UUID getHostUuid() {
        return hostUuid;
    }

    public void setHost(@Nullable UUID hostUuid) {
        this.hostUuid = hostUuid;
        sync();
    }

    @Nullable
    public UUID getGuestUuid() {
        return guestUuid;
    }

    public void setGuest(@Nullable UUID guestUuid) {
        this.guestUuid = guestUuid;
        sync();
    }

    public boolean isHost(Player player) {
        return hostUuid != null && player.getUUID().equals(hostUuid);
    }

    public boolean isGuest(Player player) {
        return guestUuid != null && player.getUUID().equals(guestUuid);
    }

    // ==================== 玩家数据 ====================

    public DuelPlayerData getHostData() {
        return hostData;
    }

    public DuelPlayerData getGuestData() {
        return guestData;
    }

    /**
     * @return 玩家对应的对局数据；未入座返回 null
     */
    @Nullable
    public DuelPlayerData getDataFor(Player player) {
        if (isHost(player)) {
            return hostData;
        }
        if (isGuest(player)) {
            return guestData;
        }
        return null;
    }

    /**
     * 提交牌组（卡包右键牌桌）。
     */
    public void submitDeck(Player player, List<ItemStack> cards) {
        DuelPlayerData data = getDataFor(player);
        if (data == null) {
            return;
        }
        data.setDeck(cards);
        data.setDeckReady(true);
        sync();
    }

    /**
     * 取消提交（潜行右键牌桌）。
     */
    public void cancelDeck(Player player) {
        DuelPlayerData data = getDataFor(player);
        if (data == null) {
            return;
        }
        data.getDeck().clear();
        data.setDeckReady(false);
        sync();
    }

    /**
     * 桌面展示用：双方已提交的牌组合并（P1-2 战场渲染改造前的兼容视图）。
     */
    public List<ItemStack> getDeck() {
        List<ItemStack> merged = new ArrayList<>(hostData.getDeck());
        merged.addAll(guestData.getDeck());
        return merged;
    }

    /**
     * 清掉客人玩家的座位 attachment（房主离场重置时调用）。
     */
    public void clearGuestSeat(@Nullable MinecraftServer server) {
        if (guestUuid != null && server != null) {
            ServerPlayer guest = server.getPlayerList().getPlayer(guestUuid);
            if (guest != null) {
                guest.setData(ModAttachments.DUEL_SEAT.get(), null);
            }
        }
    }

    /**
     * 整桌重置回 IDLE（房主离场 / 对局结束复用）。
     */
    public void resetTable() {
        this.hostUuid = null;
        this.guestUuid = null;
        this.phase = DuelPhase.IDLE;
        this.manaCap = DEFAULT_MANA_CAP;
        this.hpCap = DEFAULT_HP_CAP;
        this.hostData.reset();
        this.guestData.reset();
        sync();
    }

    // ==================== 同步 ====================

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    // ==================== NBT ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("Phase", this.phase.name());
        tag.putInt("ManaCap", this.manaCap);
        tag.putInt("HpCap", this.hpCap);
        if (this.hostUuid != null) {
            tag.putUUID("Host", this.hostUuid);
        }
        if (this.guestUuid != null) {
            tag.putUUID("Guest", this.guestUuid);
        }

        CompoundTag hostTag = new CompoundTag();
        this.hostData.save(hostTag, provider);
        tag.put("HostData", hostTag);

        CompoundTag guestTag = new CompoundTag();
        this.guestData.save(guestTag, provider);
        tag.put("GuestData", guestTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        String phaseName = tag.getString("Phase");
        try {
            this.phase = DuelPhase.valueOf(phaseName);
        } catch (IllegalArgumentException e) {
            this.phase = DuelPhase.IDLE;
        }
        this.manaCap = tag.getInt("ManaCap");
        this.hpCap = tag.getInt("HpCap");
        this.hostUuid = tag.contains("Host") ? tag.getUUID("Host") : null;
        this.guestUuid = tag.contains("Guest") ? tag.getUUID("Guest") : null;

        this.hostData.load(tag.getCompound("HostData"), provider);
        this.guestData.load(tag.getCompound("GuestData"), provider);
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
