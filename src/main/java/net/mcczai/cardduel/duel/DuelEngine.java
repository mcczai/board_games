package net.mcczai.cardduel.duel;

import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.init.ModAttachments;
import net.mcczai.cardduel.network.payload.ClientboundOpenSetupPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 对局核心逻辑（服务端权威）。
 * 所有对局状态变更都经由此类，P2 技能系统的触发点也预留在后续的
 * playCard / attack / endTurn / drawCard 等方法中。
 */
public final class DuelEngine {

    private DuelEngine() {
    }

    /**
     * 空手右键牌桌：入座 / 打开设置界面 / 触发开局。
     */
    public static InteractionResult handleTableUse(ServerPlayer player, DuelTableBlockEntity table) {
        if (!table.isDoubleTable()) {
            player.displayClientMessage(Component.translatable("cardduel.duel.need_double"), false);
            return InteractionResult.SUCCESS;
        }

        DuelSeat seat = player.getData(ModAttachments.DUEL_SEAT.get());
        if (seat != null && !seat.tablePos().equals(table.getBlockPos())) {
            player.displayClientMessage(Component.translatable("cardduel.duel.already_seated"), false);
            return InteractionResult.SUCCESS;
        }

        if (table.isHost(player)) {
            switch (table.getPhase()) {
                case SETUP ->
                        PacketDistributor.sendToPlayer(player, new ClientboundOpenSetupPayload(table.getBlockPos()));
                case WAITING ->
                        player.displayClientMessage(Component.translatable("cardduel.duel.waiting_start"), false);
                default -> {
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (table.isGuest(player)) {
            player.displayClientMessage(Component.translatable("cardduel.duel.already_guest"), false);
            return InteractionResult.SUCCESS;
        }

        switch (table.getPhase()) {
            case IDLE -> {
                table.setHost(player.getUUID());
                table.setPhase(DuelPhase.SETUP);
                player.setData(ModAttachments.DUEL_SEAT.get(), new DuelSeat(table.getBlockPos(), true));
                PacketDistributor.sendToPlayer(player, new ClientboundOpenSetupPayload(table.getBlockPos()));
                player.displayClientMessage(Component.translatable("cardduel.duel.host_seated"), false);
            }
            case SETUP, WAITING -> {
                table.setGuest(player.getUUID());
                player.setData(ModAttachments.DUEL_SEAT.get(), new DuelSeat(table.getBlockPos(), false));
                player.displayClientMessage(Component.translatable("cardduel.duel.guest_seated"), false);
            }
            default -> {
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * 潜行右键牌桌：离座 / 取消提交。
     * 对局进行中（MULLIGAN/PLAYING）不允许离座。
     */
    public static InteractionResult handleLeave(ServerPlayer player, DuelTableBlockEntity table) {
        if (!table.isHost(player) && !table.isGuest(player)) {
            // 未在本桌入座：静默返回，避免"已离座"刷屏；
            // 仅在玩家坐于其他牌桌时给出一次提示
            if (player.getData(ModAttachments.DUEL_SEAT.get()) != null) {
                player.displayClientMessage(Component.translatable("cardduel.duel.already_seated"), false);
            }
            return InteractionResult.SUCCESS;
        }

        DuelPhase phase = table.getPhase();
        if (phase == DuelPhase.MULLIGAN || phase == DuelPhase.PLAYING) {
            player.displayClientMessage(Component.translatable("cardduel.duel.cant_leave"), false);
            return InteractionResult.SUCCESS;
        }

        if (table.isHost(player)) {
            // 房主离场：清掉客人座位并整桌重置
            table.clearGuestSeat(player.getServer());
            table.resetTable();
        } else if (table.isGuest(player)) {
            table.setGuest(null);
            table.cancelDeck(player);
        }

        player.removeData(ModAttachments.DUEL_SEAT.get());
        player.displayClientMessage(Component.translatable("cardduel.duel.leave_ok"), false);
        return InteractionResult.SUCCESS;
    }
}
