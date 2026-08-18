package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 → 服务端：结束当前回合（P1-1b 由 /duel endturn 命令与未来 HUD 按钮共用）。
 */
public record ServerboundEndTurnPayload() implements CustomPacketPayload {

    public static final Type<ServerboundEndTurnPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_end_turn"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEndTurnPayload> STREAM_CODEC =
            StreamCodec.unit(new ServerboundEndTurnPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
