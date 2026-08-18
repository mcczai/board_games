package net.mcczai.cardduel.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.duel.DuelEngine;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.mcczai.cardduel.duel.DuelSeat;
import net.mcczai.cardduel.init.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.Nullable;

/**
 * /duel 命令组。P1-2 HUD 上线后保留为调试工具。
 *   /duel endturn  结束当前回合
 *   /duel status   查看本桌对局状态
 *   /duel leave    离座
 */
@EventBusSubscriber(modid = CardduelMod.MODID)
public class DuelCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("duel")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.literal("endturn")
                                .executes(ctx -> endTurn(ctx.getSource())))
                        .then(Commands.literal("status")
                                .executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("leave")
                                .executes(ctx -> leave(ctx.getSource())))
        );
    }

    private static int endTurn(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DuelTableBlockEntity table = tableFor(player);
        if (table == null) {
            source.sendFailure(Component.translatable("cardduel.duel.cmd.not_seated"));
            return 0;
        }
        if (DuelEngine.endTurn(player, table)) {
            source.sendSuccess(() -> Component.translatable("cardduel.duel.cmd.endturn_ok"), false);
        } else {
            source.sendFailure(Component.translatable("cardduel.duel.cmd.not_your_turn"));
        }
        return 1;
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DuelTableBlockEntity table = tableFor(player);
        if (table == null) {
            source.sendFailure(Component.translatable("cardduel.duel.cmd.not_seated"));
            return 0;
        }
        DuelPlayerData host = table.getHostData();
        DuelPlayerData guest = table.getGuestData();
        source.sendSuccess(() -> Component.literal(String.format(
                "phase=%s manaCap=%s hpCap=%s turn=%s active=%s",
                table.getPhase(), table.getManaCap(), table.getHpCap(),
                table.getTurnNumber(), table.getActiveUuid())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "host:  hp=%s mp=%s/%s hand=%s deck=%s fatigue=%s ready=%s",
                host.getHp(), host.getMp(), host.getMpMax(), host.getHand().size(),
                host.getDeck().size(), host.getFatigue(), host.isDeckReady())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "guest: hp=%s mp=%s/%s hand=%s deck=%s fatigue=%s ready=%s",
                guest.getHp(), guest.getMp(), guest.getMpMax(), guest.getHand().size(),
                guest.getDeck().size(), guest.getFatigue(), guest.isDeckReady())), false);
        return 1;
    }

    private static int leave(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DuelTableBlockEntity table = tableFor(player);
        if (table == null) {
            source.sendFailure(Component.translatable("cardduel.duel.cmd.not_seated"));
            return 0;
        }
        DuelEngine.handleLeave(player, table);
        return 1;
    }

    /**
     * 依据玩家座位 attachment 定位牌桌。
     */
    @Nullable
    private static DuelTableBlockEntity tableFor(ServerPlayer player) {
        DuelSeat seat = player.getData(ModAttachments.DUEL_SEAT.get());
        if (seat == null) {
            return null;
        }
        if (player.serverLevel().getBlockEntity(seat.tablePos()) instanceof DuelTableBlockEntity table) {
            return table;
        }
        return null;
    }
}
