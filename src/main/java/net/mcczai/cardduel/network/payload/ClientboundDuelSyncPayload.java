package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 服务端 → 双方客户端：对局公开状态同步。
 * 只含公开信息（手牌内容不在此包中，P1-2 另行定向发送）。
 */
public record ClientboundDuelSyncPayload(
        String phase,
        int manaCap,
        int hpCap,
        int turnNumber,
        @Nullable UUID activeUuid,
        @Nullable UUID hostUuid,
        @Nullable UUID guestUuid,
        PlayerSyncView host,
        PlayerSyncView guest) implements CustomPacketPayload {

    public static final Type<ClientboundDuelSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDuelSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ClientboundDuelSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    String phase = ByteBufCodecs.STRING_UTF8.decode(buf);
                    int manaCap = buf.readVarInt();
                    int hpCap = buf.readVarInt();
                    int turnNumber = buf.readVarInt();
                    UUID active = readNullableUuid(buf);
                    UUID host = readNullableUuid(buf);
                    UUID guest = readNullableUuid(buf);
                    PlayerSyncView hostView = PlayerSyncView.STREAM_CODEC.decode(buf);
                    PlayerSyncView guestView = PlayerSyncView.STREAM_CODEC.decode(buf);
                    return new ClientboundDuelSyncPayload(phase, manaCap, hpCap, turnNumber,
                            active, host, guest, hostView, guestView);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ClientboundDuelSyncPayload payload) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, payload.phase);
                    buf.writeVarInt(payload.manaCap);
                    buf.writeVarInt(payload.hpCap);
                    buf.writeVarInt(payload.turnNumber);
                    writeNullableUuid(buf, payload.activeUuid);
                    writeNullableUuid(buf, payload.hostUuid);
                    writeNullableUuid(buf, payload.guestUuid);
                    PlayerSyncView.STREAM_CODEC.encode(buf, payload.host);
                    PlayerSyncView.STREAM_CODEC.encode(buf, payload.guest);
                }

                private static void writeNullableUuid(RegistryFriendlyByteBuf buf, @Nullable UUID uuid) {
                    buf.writeBoolean(uuid != null);
                    if (uuid != null) {
                        buf.writeUUID(uuid);
                    }
                }

                @Nullable
                private static UUID readNullableUuid(RegistryFriendlyByteBuf buf) {
                    return buf.readBoolean() ? buf.readUUID() : null;
                }
            };

    /**
     * 从牌桌实体构建同步包。
     */
    public static ClientboundDuelSyncPayload of(DuelTableBlockEntity table) {
        return new ClientboundDuelSyncPayload(
                table.getPhase().name(),
                table.getManaCap(),
                table.getHpCap(),
                table.getTurnNumber(),
                table.getActiveUuid(),
                table.getHostUuid(),
                table.getGuestUuid(),
                PlayerSyncView.of(table.getHostData()),
                PlayerSyncView.of(table.getGuestData())
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 单个玩家的公开状态视图。
     */
    public record PlayerSyncView(int hp, int mp, int mpMax, int handCount, int deckCount, int fatigue,
                                 boolean deckReady) {

        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSyncView> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public PlayerSyncView decode(RegistryFriendlyByteBuf buf) {
                        return new PlayerSyncView(
                                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                                buf.readBoolean());
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buf, PlayerSyncView view) {
                        buf.writeVarInt(view.hp);
                        buf.writeVarInt(view.mp);
                        buf.writeVarInt(view.mpMax);
                        buf.writeVarInt(view.handCount);
                        buf.writeVarInt(view.deckCount);
                        buf.writeVarInt(view.fatigue);
                        buf.writeBoolean(view.deckReady);
                    }
                };

        public static PlayerSyncView of(DuelPlayerData data) {
            return new PlayerSyncView(
                    data.getHp(), data.getMp(), data.getMpMax(),
                    data.getHand().size(), data.getDeck().size(), data.getFatigue(),
                    data.isDeckReady());
        }
    }
}
