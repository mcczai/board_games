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
 * 服务端 → 陷阱本人：秘密区内容（隐藏信息，绝不发给对手）。
 */
public record ClientboundDuelTrapPayload(List<ItemStack> traps) implements CustomPacketPayload {

    public static final Type<ClientboundDuelTrapPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_trap"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDuelTrapPayload> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(ClientboundDuelTrapPayload::new, ClientboundDuelTrapPayload::traps);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
