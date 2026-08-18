package net.mcczai.cardduel.event;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.duel.DuelEngine;
import net.mcczai.cardduel.duel.DuelSeat;
import net.mcczai.cardduel.init.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 玩家相关事件：掉线判负 / 等待阶段按离座处理。
 */
@EventBusSubscriber(modid = CardduelMod.MODID)
public class DuelPlayerEvents {

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DuelSeat seat = player.getData(ModAttachments.DUEL_SEAT.get());
        if (seat == null) {
            return;
        }
        if (player.serverLevel().getBlockEntity(seat.tablePos()) instanceof DuelTableBlockEntity table) {
            DuelEngine.handleDisconnect(player, table);
        } else {
            // 牌桌已不存在：直接清理残留座位
            player.removeData(ModAttachments.DUEL_SEAT.get());
        }
    }
}
