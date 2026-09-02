package net.mcczai.cardduel.client.hud;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.client.duel.ClientDuelState;
import net.mcczai.cardduel.client.duel.DuelCameraManager;
import net.mcczai.cardduel.client.duel.DuelInteraction;
import net.mcczai.cardduel.client.duel.HudClickManager;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 对局 HUD：屏幕上方双方状态条（HP/法力/牌库/疲劳/秘密区/装备/图腾）+ 中央回合指示 + 右下"结束回合"按钮。
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class BattleBoardHud {

    private static final int BAR_W = 200;
    private static final int BAR_H = 64;
    private static final int MARGIN = 8;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        ClientboundDuelSyncPayload sync = ClientDuelState.get();
        if (sync == null || !DuelCameraManager.isActivePhase(sync.phase())) {
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth();

        boolean isHost = mc.player.getUUID().equals(sync.hostUuid());
        ClientboundDuelSyncPayload.PlayerSyncView me = isHost ? sync.host() : sync.guest();
        ClientboundDuelSyncPayload.PlayerSyncView foe = isHost ? sync.guest() : sync.host();
        String meName = isHost ? sync.hostName() : sync.guestName();
        String foeName = isHost ? sync.guestName() : sync.hostName();
        boolean myTurn = mc.player.getUUID().equals(sync.activeUuid());

        // 装备数从本地牌桌实体读取（装备为公开信息，随方块实体同步）
        int meEquip = 0;
        int foeEquip = 0;
        if (mc.level != null && mc.level.getBlockEntity(sync.tablePos()) instanceof DuelTableBlockEntity table) {
            DuelPlayerData myData = isHost ? table.getHostData() : table.getGuestData();
            DuelPlayerData foeData = isHost ? table.getGuestData() : table.getHostData();
            meEquip = countEquipped(myData);
            foeEquip = countEquipped(foeData);
        }

        renderPlayerBar(g, MARGIN, MARGIN, meName, me, sync, myTurn, meEquip);
        renderPlayerBar(g, sw - BAR_W - MARGIN, MARGIN, foeName, foe, sync, !myTurn, foeEquip);

        String turn = Component.translatable("cardduel.hud.turn_total",
                sync.turnNumber(), sync.turnLimit()).getString();
        // 剩余回合 ≤5 时以警告色提示（回合上限保险丝即将触发）
        int turnColor = sync.turnLimit() - sync.turnNumber() <= 5 ? 0xFFFF7043 : 0xFFFFFFFF;
        g.drawCenteredString(mc.font, turn, sw / 2, MARGIN + 4, turnColor);

        // 选中 / 换牌提示
        if ("MULLIGAN".equals(sync.phase())) {
            String hint = Component.translatable("cardduel.hud.mulligan_hint",
                    DuelInteraction.getMulliganSelection().size()).getString();
            g.drawCenteredString(mc.font, hint, sw / 2, MARGIN + 20, 0xFFFFD54F);
        } else if (DuelInteraction.getSelectedHand() >= 0) {
            String hint = switch (String.valueOf(HudClickManager.selectedHandKind())) {
                case "mana", "secret" -> Component.translatable("cardduel.hud.selected_mana").getString();
                case "anvil" -> Component.translatable("cardduel.hud.selected_anvil").getString();
                case "equip" -> Component.translatable("cardduel.hud.selected_equip").getString();
                default -> Component.translatable("cardduel.hud.selected_hand").getString();
            };
            g.drawCenteredString(mc.font, hint, sw / 2, MARGIN + 20, 0xFF81C784);
        } else if (DuelInteraction.getSelectedBoard() >= 0) {
            g.drawCenteredString(mc.font, Component.translatable("cardduel.hud.selected_board").getString(),
                    sw / 2, MARGIN + 20, 0xFFFFD54F);
        }

        HudClickManager.renderEndTurnButton(g, myTurn, "MULLIGAN".equals(sync.phase()));
    }

    private static int countEquipped(DuelPlayerData data) {
        int count = 0;
        for (ItemStack stack : data.getEquipped()) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static void renderPlayerBar(GuiGraphics g, int x, int y, String name,
                                        ClientboundDuelSyncPayload.PlayerSyncView view,
                                        ClientboundDuelSyncPayload sync, boolean active, int equipCount) {
        Minecraft mc = Minecraft.getInstance();
        int border = active ? 0xFFFFD54F : 0xFF555555;
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, border);
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xC0202020);

        g.drawString(mc.font, name, x + 6, y + 4, 0xFFFFFFFF);
        g.drawString(mc.font,
                Component.translatable("cardduel.hud.hp", view.hp(), sync.hpCap()).getString(),
                x + 6, y + 16, 0xFFE57373);
        g.drawString(mc.font,
                Component.translatable("cardduel.hud.mp", view.mp(), view.mpMax()).getString(),
                x + 6, y + 28, 0xFF64B5F6);
        g.drawString(mc.font,
                Component.translatable("cardduel.hud.deck_fatigue", view.deckCount(), view.fatigue()).getString(),
                x + 6, y + 40, 0xFFBDBDBD);
        String extras = Component.translatable("cardduel.hud.trap_equip", view.trapCount(), equipCount).getString();
        if (view.totemActive()) {
            extras += " " + Component.translatable("cardduel.hud.totem").getString();
        }
        g.drawString(mc.font, extras, x + 6, y + 52, 0xFFCE93D8);
    }
}
