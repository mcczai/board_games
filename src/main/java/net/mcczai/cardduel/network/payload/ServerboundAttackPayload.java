package net.mcczai.cardduel.network.payload;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 → 服务端：己方召唤卡攻击。
 * targetSlot >= 0 攻击对方战场槽位；targetSlot = -1 攻击对方玩家（打脸）。
 */
public record ServerboundAttackPayload(int attackerSlot, int targetSlot) implements CustomPacketPayload {

    public static final Type<ServerboundAttackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID, "duel_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAttackPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundAttackPayload::attackerSlot,
                    ByteBufCodecs.VAR_INT, ServerboundAttackPayload::targetSlot,
                    ServerboundAttackPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
