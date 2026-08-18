package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端 → 服务端：换牌确认（要换掉的手牌索引，≤2 个）。
 */
public record ServerboundMulliganPayload(List<Integer> indices) implements CustomPacketPayload {

    public static final Type<ServerboundMulliganPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_mulligan"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMulliganPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ServerboundMulliganPayload decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readVarInt();
                    List<Integer> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        list.add(buf.readVarInt());
                    }
                    return new ServerboundMulliganPayload(list);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ServerboundMulliganPayload payload) {
                    buf.writeVarInt(payload.indices.size());
                    for (int index : payload.indices) {
                        buf.writeVarInt(index);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
