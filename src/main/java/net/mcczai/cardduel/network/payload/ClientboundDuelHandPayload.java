package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 服务端 → 手牌本人：手牌内容（隐藏信息，绝不发给对手）。
 */
public record ClientboundDuelHandPayload(List<ItemStack> hand) implements CustomPacketPayload {

    public static final Type<ClientboundDuelHandPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_hand"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDuelHandPayload> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(ClientboundDuelHandPayload::new, ClientboundDuelHandPayload::hand);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
