package net.mcczai.cardduel.network;

import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.duel.DuelPhase;
import net.mcczai.cardduel.network.payload.ServerboundSetupPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 网络包注册与处理器（公共侧）。
 * 客户端侧处理见 client/event/ClientPayloadHandlers。
 */
public final class DuelNet {

    private DuelNet() {
    }

    /**
     * 服务端方向（客户端 → 服务端）的包注册，在 Mod 构造器中调用。
     */
    public static void registerServer(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(ServerboundSetupPayload.TYPE, ServerboundSetupPayload.STREAM_CODEC, DuelNet::handleSetup);
    }

    /**
     * 房主提交对局上限：校验座位/阶段后写入牌桌并进入 WAITING。
     */
    private static void handleSetup(ServerboundSetupPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            // 距离校验，防止跨世界/超远伪造
            if (serverPlayer.distanceToSqr(payload.tablePos().getCenter()) > 64.0D * 64.0D) {
                return;
            }
            if (serverPlayer.serverLevel().getBlockEntity(payload.tablePos()) instanceof DuelTableBlockEntity table
                    && table.isHost(serverPlayer)
                    && table.getPhase() == DuelPhase.SETUP) {
                table.setCaps(
                        Mth.clamp(payload.manaCap(), 1, 99),
                        Mth.clamp(payload.hpCap(), 1, 999)
                );
                table.setPhase(DuelPhase.WAITING);
                serverPlayer.displayClientMessage(
                        Component.translatable("cardduel.duel.caps_set", table.getManaCap(), table.getHpCap()),
                        false
                );
            }
        });
    }
}
