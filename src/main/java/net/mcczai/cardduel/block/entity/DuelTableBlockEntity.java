package net.mcczai.cardduel.block.entity;

import net.mcczai.cardduel.block.DuelTableBlock;
import net.mcczai.cardduel.duel.DuelPhase;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.mcczai.cardduel.init.ModAttachments;
import net.mcczai.cardduel.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
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

    /** 全局轮次（提示与行动顺序用；每回合 +1） */
    private int turnNumber;
    /** 当前行动方 */
    @Nullable
    private UUID activeUuid;

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

    // ==================== 回合 ====================

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
        sync();
    }

    @Nullable
    public UUID getActiveUuid() {
        return activeUuid;
    }

    public void setActiveUuid(@Nullable UUID activeUuid) {
        this.activeUuid = activeUuid;
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
     * 是否为双人牌桌（两张牌桌并排放置）。
     */
    public boolean isDoubleTable() {
        return this.getBlockState().getValue(DuelTableBlock.DOUBLE);
    }

    /**
     * 牌桌被破坏时清理双方座位 attachment 并通知玩家。
     * 方块实体数据随方块一起销毁，无需写回。
     */
    public void clearSeatsOnBreak(@Nullable MinecraftServer server) {
        if (server == null) {
            return;
        }
        if (hostUuid != null) {
            ServerPlayer host = server.getPlayerList().getPlayer(hostUuid);
            if (host != null) {
                host.removeData(ModAttachments.DUEL_SEAT.get());
                host.displayClientMessage(Component.translatable("cardduel.duel.table_broken"), false);
            }
        }
        if (guestUuid != null) {
            ServerPlayer guest = server.getPlayerList().getPlayer(guestUuid);
            if (guest != null) {
                guest.removeData(ModAttachments.DUEL_SEAT.get());
                guest.displayClientMessage(Component.translatable("cardduel.duel.table_broken"), false);
            }
        }
    }

    /**
     * 清掉客人玩家的座位 attachment（房主离场重置时调用）。
     */
    public void clearGuestSeat(@Nullable MinecraftServer server) {
        if (guestUuid != null && server != null) {
            ServerPlayer guest = server.getPlayerList().getPlayer(guestUuid);
            if (guest != null) {
                guest.removeData(ModAttachments.DUEL_SEAT.get());
            }
        }
    }

    /**
     * 整桌重置回 IDLE（房主离场时调用）。
     */
    public void resetTable() {
        this.hostUuid = null;
        this.guestUuid = null;
        this.phase = DuelPhase.IDLE;
        this.manaCap = DEFAULT_MANA_CAP;
        this.hpCap = DEFAULT_HP_CAP;
        this.hostData.reset();
        this.guestData.reset();
        this.turnNumber = 0;
        this.activeUuid = null;
        sync();
    }

    /**
     * 对局结束：清空对局数据，保留座位与上限设置，回到 WAITING 可直接下一局。
     */
    public void resetDuel() {
        this.hostData.reset();
        this.guestData.reset();
        this.turnNumber = 0;
        this.activeUuid = null;
        this.phase = DuelPhase.WAITING;
        sync();
    }

    // ==================== 同步 ====================

    public void sync() {
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
        savePublic(tag, provider);

        // 全量数据（含手牌）落盘，覆盖上面写入的公开视图
        CompoundTag hostTag = new CompoundTag();
        this.hostData.save(hostTag, provider);
        tag.put("HostData", hostTag);

        CompoundTag guestTag = new CompoundTag();
        this.guestData.save(guestTag, provider);
        tag.put("GuestData", guestTag);
    }

    /**
     * 只写公开数据（不含手牌内容），供同步包使用。
     */
    private void savePublic(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("Phase", this.phase.name());
        tag.putInt("ManaCap", this.manaCap);
        tag.putInt("HpCap", this.hpCap);
        tag.putInt("TurnNumber", this.turnNumber);
        if (this.activeUuid != null) {
            tag.putUUID("Active", this.activeUuid);
        }
        if (this.hostUuid != null) {
            tag.putUUID("Host", this.hostUuid);
        }
        if (this.guestUuid != null) {
            tag.putUUID("Guest", this.guestUuid);
        }

        CompoundTag hostTag = new CompoundTag();
        this.hostData.savePublic(hostTag, provider);
        tag.put("HostData", hostTag);

        CompoundTag guestTag = new CompoundTag();
        this.guestData.savePublic(guestTag, provider);
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
        this.turnNumber = tag.getInt("TurnNumber");
        this.activeUuid = tag.contains("Active") ? tag.getUUID("Active") : null;
        this.hostUuid = tag.contains("Host") ? tag.getUUID("Host") : null;
        this.guestUuid = tag.contains("Guest") ? tag.getUUID("Guest") : null;

        this.hostData.load(tag.getCompound("HostData"), provider);
        this.guestData.load(tag.getCompound("GuestData"), provider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        // 同步包只含公开数据：手牌内容经 ClientboundDuelHandPayload 定向发给本人
        CompoundTag tag = new CompoundTag();
        savePublic(tag, provider);
        return tag;
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
