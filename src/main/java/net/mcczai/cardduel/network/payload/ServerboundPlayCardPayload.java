package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 → 服务端：打出手牌（handIndex）到己方战场槽位（boardSlot）。
 */
public record ServerboundPlayCardPayload(int handIndex, int boardSlot) implements CustomPacketPayload {

    public static final Type<ServerboundPlayCardPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_play_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPlayCardPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundPlayCardPayload::handIndex,
                    ByteBufCodecs.VAR_INT, ServerboundPlayCardPayload::boardSlot,
                    ServerboundPlayCardPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
