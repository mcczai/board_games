package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 服务端 → 房主客户端：打开对局设置界面。
 */
public record ClientboundOpenSetupPayload(BlockPos tablePos) implements CustomPacketPayload {

    public static final Type<ClientboundOpenSetupPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "open_setup"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenSetupPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ClientboundOpenSetupPayload::tablePos,
                    ClientboundOpenSetupPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
