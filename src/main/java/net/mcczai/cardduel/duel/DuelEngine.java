package net.mcczai.cardduel.duel;

import net.mcczai.cardduel.API.item.nbt.CardDataAccessor;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.init.ModAttachments;
import net.mcczai.cardduel.init.ModItem;
import net.mcczai.cardduel.items.ICard;
import net.mcczai.cardduel.network.payload.ClientboundDuelHandPayload;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.mcczai.cardduel.network.payload.ClientboundOpenSetupPayload;
import net.mcczai.cardduel.resources.DefaultAssets;
import net.mcczai.cardduel.skill.SkillHooks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 对局核心逻辑（服务端权威）。
 * 所有对局状态变更都经由此类；P2 技能系统的触发点见 SkillHooks。
 */
public final class DuelEngine {

    private DuelEngine() {
    }

    // ==================== 入座 / 离座 ====================

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
                case WAITING -> tryStart(player, table);
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
            // 未在本桌入座：静默返回，避免刷屏；仅在坐于别桌时提示
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
        syncToPlayers(table);
        return InteractionResult.SUCCESS;
    }

    /**
     * 掉线处理：对局中判负；等待阶段按离座处理。
     */
    public static void handleDisconnect(ServerPlayer player, DuelTableBlockEntity table) {
        DuelPhase phase = table.getPhase();
        if (phase == DuelPhase.MULLIGAN || phase == DuelPhase.PLAYING) {
            UUID winnerUuid = table.isHost(player) ? table.getGuestUuid() : table.getHostUuid();
            broadcast(table, Component.translatable("cardduel.duel.disconnect", player.getGameProfile().getName()));
            if (winnerUuid != null) {
                broadcast(table, Component.translatable("cardduel.duel.finish", playerName(table, winnerUuid)));
            }
            player.removeData(ModAttachments.DUEL_SEAT.get());
            table.resetDuel();
            syncToPlayers(table);
        } else {
            handleLeave(player, table);
        }
    }

    // ==================== 开局 ====================

    /**
     * 房主触发开局：校验 → 洗牌 → 起手 4 张 → 随机先手 → PLAYING。
     */
    public static boolean tryStart(ServerPlayer host, DuelTableBlockEntity table) {
        if (!table.isHost(host) || table.getPhase() != DuelPhase.WAITING) {
            return false;
        }
        if (!table.isDoubleTable()) {
            host.displayClientMessage(Component.translatable("cardduel.duel.need_double"), false);
            return false;
        }
        UUID guestUuid = table.getGuestUuid();
        if (guestUuid == null) {
            host.displayClientMessage(Component.translatable("cardduel.duel.start_need_guest"), false);
            return false;
        }
        ServerPlayer guest = host.getServer().getPlayerList().getPlayer(guestUuid);
        if (guest == null) {
            host.displayClientMessage(Component.translatable("cardduel.duel.start_guest_offline"), false);
            return false;
        }

        DuelPlayerData hostData = table.getHostData();
        DuelPlayerData guestData = table.getGuestData();
        if (!hostData.isDeckReady() || !guestData.isDeckReady()) {
            host.displayClientMessage(Component.translatable("cardduel.duel.start_need_decks"), false);
            return false;
        }

        Collections.shuffle(hostData.getDeck());
        Collections.shuffle(guestData.getDeck());

        hostData.setMulliganDone(false);
        guestData.setMulliganDone(false);

        hostData.setHp(table.getHpCap());
        guestData.setHp(table.getHpCap());
        hostData.setTurnCount(0);
        guestData.setTurnCount(0);

        boolean hostFirst = host.getRandom().nextBoolean();
        UUID firstUuid = hostFirst ? host.getUUID() : guestUuid;
        table.setActiveUuid(firstUuid);
        table.setTurnNumber(0);

        // 起手 4 张
        for (int i = 0; i < 4; i++) {
            drawCard(table, hostData, host.getGameProfile().getName());
            drawCard(table, guestData, guest.getGameProfile().getName());
        }
        // 后手获得硬币卡
        DuelPlayerData secondData = hostFirst ? guestData : hostData;
        secondData.getHand().add(createCoin());

        String firstName = hostFirst ? host.getGameProfile().getName() : guest.getGameProfile().getName();
        table.setPhase(DuelPhase.MULLIGAN);

        broadcast(table, Component.translatable("cardduel.duel.start", firstName));
        broadcast(table, Component.translatable("cardduel.duel.mulligan_prompt"));
        syncToPlayers(table);
        return true;
    }

    /**
     * 后手硬币卡：使用后本回合 +1 法力并消失。
     */
    private static ItemStack createCoin() {
        ItemStack coin = new ItemStack(ModItem.CARD_ITEM.get());
        if (coin.getItem() instanceof CardDataAccessor accessor) {
            accessor.setCardId(coin, DefaultAssets.COIN_CARD_ID);
        }
        return coin;
    }

    // ==================== 回合 ====================

    /**
     * 当前行动方的新回合开始：回合数 +1 → MP 上限 = min(该玩家回合数, 封顶) 并回满 → 抽 1 张。
     */
    public static void startTurn(DuelTableBlockEntity table) {
        UUID activeUuid = table.getActiveUuid();
        if (activeUuid == null) {
            return;
        }
        DuelPlayerData active = activeData(table);
        table.setTurnNumber(table.getTurnNumber() + 1);
        active.setTurnCount(active.getTurnCount() + 1);

        int mpMax = Math.min(active.getTurnCount(), table.getManaCap());
        active.setMpMax(mpMax);
        active.setMp(mpMax);

        SkillHooks.onTurnStart(table, active);
        drawCard(table, active, playerName(table, activeUuid));

        broadcast(table, Component.translatable("cardduel.duel.turn_start",
                table.getTurnNumber(), playerName(table, activeUuid), active.getMp(), active.getMpMax()));
        syncToPlayers(table);
    }

    /**
     * 结束当前回合并切换到对方。
     */
    public static boolean endTurn(ServerPlayer player, DuelTableBlockEntity table) {
        if (table.getPhase() != DuelPhase.PLAYING) {
            return false;
        }
        if (!player.getUUID().equals(table.getActiveUuid())) {
            player.displayClientMessage(Component.translatable("cardduel.duel.cmd.not_your_turn"), false);
            return false;
        }
        UUID otherUuid = table.isHost(player) ? table.getGuestUuid() : table.getHostUuid();
        if (otherUuid == null) {
            return false;
        }

        SkillHooks.onTurnEnd(table, activeData(table));
        table.setActiveUuid(otherUuid);
        startTurn(table);
        return true;
    }

    // ==================== 抽牌 / 伤害 / 胜负 ====================

    /**
     * 抽 1 张牌：牌库空 → 疲劳扣血；手牌满 → 烧入弃牌堆。
     */
    public static void drawCard(DuelTableBlockEntity table, DuelPlayerData data, String playerName) {
        if (!data.getDeck().isEmpty()) {
            ItemStack card = data.getDeck().remove(0);
            if (data.getHand().size() < DuelPlayerData.MAX_HAND) {
                data.getHand().add(card);
                broadcast(table, Component.translatable("cardduel.duel.draw", playerName, data.getDeck().size()));
            } else {
                data.getDiscard().add(card);
                broadcast(table, Component.translatable("cardduel.duel.hand_full_burn", playerName));
            }
            SkillHooks.onDraw(table, data, card);
        } else {
            data.setFatigue(data.getFatigue() + 1);
            broadcast(table, Component.translatable("cardduel.duel.fatigue",
                    playerName, data.getFatigue(), data.getFatigue()));
            dealPlayerDamage(table, data, data.getFatigue());
        }
    }

    /**
     * 对玩家造成伤害并判定胜负。
     */
    public static void dealPlayerDamage(DuelTableBlockEntity table, DuelPlayerData target, int amount) {
        if (target.getHp() <= 0) {
            return;
        }
        target.setHp(target.getHp() - amount);
        SkillHooks.onPlayerDamaged(table, target, amount);
        syncToPlayers(table);
        if (target.getHp() <= 0) {
            UUID winnerUuid = target == table.getHostData() ? table.getGuestUuid() : table.getHostUuid();
            finishDuel(table, winnerUuid);
        }
    }

    /**
     * 对局结束：宣布胜者，清场并回到 WAITING（保留座位与上限，可直接下一局）。
     */
    public static void finishDuel(DuelTableBlockEntity table, @Nullable UUID winnerUuid) {
        if (table.getPhase() != DuelPhase.PLAYING) {
            return;
        }
        String winnerName = winnerUuid == null ? "?" : playerName(table, winnerUuid);
        broadcast(table, Component.translatable("cardduel.duel.finish", winnerName));
        table.resetDuel();
        syncToPlayers(table);
    }

    /**
     * 牌组提交后检查双方是否都已就绪。
     */
    public static void notifyDeckReady(DuelTableBlockEntity table) {
        if (table.getPhase() == DuelPhase.WAITING
                && table.getHostUuid() != null
                && table.getGuestUuid() != null
                && table.getHostData().isDeckReady()
                && table.getGuestData().isDeckReady()) {
            broadcast(table, Component.translatable("cardduel.duel.both_ready"));
        }
    }

    // ==================== 出牌 / 攻击 ====================

    /**
     * 打出手牌到己方战场槽位。
     */
    public static boolean playCard(ServerPlayer player, DuelTableBlockEntity table, int handIndex, int boardSlot) {
        if (table.getPhase() != DuelPhase.PLAYING) {
            return false;
        }
        if (!player.getUUID().equals(table.getActiveUuid())) {
            player.displayClientMessage(Component.translatable("cardduel.duel.cmd.not_your_turn"), false);
            return false;
        }
        DuelPlayerData data = activeData(table);
        if (handIndex < 0 || handIndex >= data.getHand().size()) {
            return false;
        }
        if (boardSlot < 0 || boardSlot >= DuelPlayerData.BOARD_SIZE) {
            return false;
        }
        if (!data.getBoard()[boardSlot].isEmpty()) {
            return false;
        }

        ItemStack card = data.getHand().remove(handIndex);
        ICard iCard = ICard.getICardOrNull(card);
        if (iCard == null) {
            data.getHand().add(handIndex, card);
            return false;
        }

        // 硬币卡：本回合 +1 法力并消失（不占槽、不耗 MP）
        if (DefaultAssets.COIN_CARD_ID.equals(iCard.getCardId(card))) {
            data.setMp(Math.min(data.getMpMax(), data.getMp() + 1));
            syncToPlayers(table);
            broadcast(table, Component.translatable("cardduel.duel.coin_used",
                    playerName(table, player.getUUID())));
            return true;
        }

        int cost = iCard.getMP(card);
        if (data.getMp() < cost) {
            data.getHand().add(handIndex, card);
            player.displayClientMessage(Component.translatable("cardduel.duel.not_enough_mp"), false);
            return false;
        }

        data.setMp(data.getMp() - cost);
        data.getBoard()[boardSlot] = card;
        data.getSummonTurn()[boardSlot] = data.getTurnCount();
        SkillHooks.onSummoned(table, data, boardSlot, card);
        table.sync();
        syncToPlayers(table);
        broadcast(table, Component.translatable("cardduel.duel.card_played",
                playerName(table, player.getUUID())));
        return true;
    }

    /**
     * 己方召唤卡攻击：targetSlot >= 0 打对方召唤卡（炉石式互伤），-1 打脸。
     */
    public static boolean attack(ServerPlayer player, DuelTableBlockEntity table, int attackerSlot, int targetSlot) {
        if (table.getPhase() != DuelPhase.PLAYING) {
            return false;
        }
        if (!player.getUUID().equals(table.getActiveUuid())) {
            player.displayClientMessage(Component.translatable("cardduel.duel.cmd.not_your_turn"), false);
            return false;
        }
        DuelPlayerData attackerData = activeData(table);
        DuelPlayerData targetData = attackerData == table.getHostData() ? table.getGuestData() : table.getHostData();
        if (attackerSlot < 0 || attackerSlot >= DuelPlayerData.BOARD_SIZE) {
            return false;
        }
        ItemStack attackerCard = attackerData.getBoard()[attackerSlot];
        ICard attackerAccess = ICard.getICardOrNull(attackerCard);
        if (attackerAccess == null) {
            return false;
        }
        int turnCount = attackerData.getTurnCount();
        if (attackerData.getSummonTurn()[attackerSlot] == turnCount) {
            player.displayClientMessage(Component.translatable("cardduel.duel.summon_sickness"), false);
            return false;
        }
        if (attackerData.getAttackTurn()[attackerSlot] == turnCount) {
            player.displayClientMessage(Component.translatable("cardduel.duel.already_attacked"), false);
            return false;
        }

        int atk = attackerAccess.getATK(attackerCard);

        if (targetSlot < 0) {
            // 打脸
            attackerData.getAttackTurn()[attackerSlot] = turnCount;
            dealPlayerDamage(table, targetData, atk);
        } else {
            if (targetSlot >= DuelPlayerData.BOARD_SIZE) {
                return false;
            }
            ItemStack targetCard = targetData.getBoard()[targetSlot];
            ICard targetAccess = ICard.getICardOrNull(targetCard);
            if (targetAccess == null) {
                return false;
            }
            attackerData.getAttackTurn()[attackerSlot] = turnCount;
            SkillHooks.onAttack(table, attackerData, attackerSlot, attackerCard,
                    targetData, targetSlot, targetCard);
            applyCardDamage(table, targetData, targetSlot, targetCard, atk);
            applyCardDamage(table, attackerData, attackerSlot, attackerCard, targetAccess.getATK(targetCard));
        }
        table.sync();
        syncToPlayers(table);
        return true;
    }

    /**
     * 对战场卡牌造成伤害：HP ≤ 0 则清槽并进弃牌堆。
     */
    private static void applyCardDamage(DuelTableBlockEntity table, DuelPlayerData owner, int slot,
                                        ItemStack card, int damage) {
        ICard access = ICard.getICardOrNull(card);
        if (access == null) {
            return;
        }
        int newHp = access.getHP(card) - damage;
        if (newHp <= 0) {
            owner.getBoard()[slot] = ItemStack.EMPTY;
            owner.getSummonTurn()[slot] = 0;
            owner.getAttackTurn()[slot] = 0;
            owner.getDiscard().add(card);
            SkillHooks.onDeath(table, owner, slot, card);
        } else {
            access.setHP(card, newHp);
        }
    }

    // ==================== 换牌 ====================

    /**
     * 换牌确认：把选中（≤2 张、不可含硬币）放回牌库底，抽等量新牌。
     */
    public static boolean mulligan(ServerPlayer player, DuelTableBlockEntity table, List<Integer> indices) {
        if (table.getPhase() != DuelPhase.MULLIGAN) {
            return false;
        }
        DuelPlayerData data = table.getDataFor(player);
        if (data == null || data.isMulliganDone()) {
            return false;
        }
        Set<Integer> set = new HashSet<>(indices);
        if (set.size() != indices.size() || set.size() > 2) {
            return false;
        }
        for (int i : set) {
            if (i < 0 || i >= data.getHand().size()) {
                return false;
            }
            ItemStack card = data.getHand().get(i);
            ICard access = ICard.getICardOrNull(card);
            if (access != null && DefaultAssets.COIN_CARD_ID.equals(access.getCardId(card))) {
                return false; // 硬币不可换
            }
        }

        List<Integer> sorted = new ArrayList<>(set);
        sorted.sort(Collections.reverseOrder());
        for (int i : sorted) {
            data.getDeck().add(data.getHand().remove(i));
        }
        String name = playerName(table, player.getUUID());
        for (int i = 0; i < sorted.size(); i++) {
            drawCard(table, data, name);
        }

        data.setMulliganDone(true);
        checkMulliganComplete(table);
        syncToPlayers(table);
        return true;
    }

    /**
     * 双方都完成换牌 → 进入 PLAYING 并开始先手回合。
     */
    private static void checkMulliganComplete(DuelTableBlockEntity table) {
        if (table.getHostData().isMulliganDone() && table.getGuestData().isMulliganDone()) {
            table.setPhase(DuelPhase.PLAYING);
            broadcast(table, Component.translatable("cardduel.duel.mulligan_done"));
            startTurn(table);
        }
    }

    // ==================== 同步与工具 ====================

    /**
     * 把对局公开状态推送给双方玩家（P1-2 HUD 消费），并把各自的手牌定向发给本人。
     */
    public static void syncToPlayers(DuelTableBlockEntity table) {
        if (!(table.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        ClientboundDuelSyncPayload payload = ClientboundDuelSyncPayload.of(table);
        UUID[] uuids = {table.getHostUuid(), table.getGuestUuid()};
        DuelPlayerData[] datas = {table.getHostData(), table.getGuestData()};
        for (int i = 0; i < uuids.length; i++) {
            UUID uuid = uuids[i];
            if (uuid == null) {
                continue;
            }
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                PacketDistributor.sendToPlayer(player, payload);
                PacketDistributor.sendToPlayer(player, new ClientboundDuelHandPayload(datas[i].getHand()));
            }
        }
    }

    private static void broadcast(DuelTableBlockEntity table, Component message) {
        if (!(table.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID[] uuids = {table.getHostUuid(), table.getGuestUuid()};
        for (UUID uuid : uuids) {
            if (uuid == null) {
                continue;
            }
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.displayClientMessage(message, false);
            }
        }
    }

    private static DuelPlayerData activeData(DuelTableBlockEntity table) {
        if (table.getActiveUuid() != null && table.getActiveUuid().equals(table.getHostUuid())) {
            return table.getHostData();
        }
        return table.getGuestData();
    }

    private static String playerName(DuelTableBlockEntity table, UUID uuid) {
        if (table.getLevel() instanceof ServerLevel serverLevel) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                return player.getGameProfile().getName();
            }
        }
        return "?";
    }
}
