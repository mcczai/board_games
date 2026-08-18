package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 房主客户端 → 服务端：提交对局上限（法力封顶、生命上限）。
 */
public record ServerboundSetupPayload(BlockPos tablePos, int manaCap, int hpCap) implements CustomPacketPayload {

    public static final Type<ServerboundSetupPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_setup"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetupPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ServerboundSetupPayload::tablePos,
                    ByteBufCodecs.VAR_INT, ServerboundSetupPayload::manaCap,
                    ByteBufCodecs.VAR_INT, ServerboundSetupPayload::hpCap,
                    ServerboundSetupPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
