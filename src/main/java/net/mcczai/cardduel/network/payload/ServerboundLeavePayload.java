package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 → 服务端：离座（退出准备）。
 * 服务端依据玩家自身的座位 attachment 定位牌桌。
 */
public record ServerboundLeavePayload() implements CustomPacketPayload {

    public static final Type<ServerboundLeavePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_leave"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLeavePayload> STREAM_CODEC =
            StreamCodec.unit(new ServerboundLeavePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
