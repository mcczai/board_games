package net.mcczai.cardduel.client.hud;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.client.duel.ClientDuelState;
import net.mcczai.cardduel.client.duel.DuelCameraManager;
import net.mcczai.cardduel.client.duel.HudClickManager;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 对局 HUD：屏幕上方双方状态条（HP/法力/牌库/疲劳）+ 中央回合指示 + 右下"结束回合"按钮。
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class BattleBoardHud {

    private static final int BAR_W = 200;
    private static final int BAR_H = 52;
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

        renderPlayerBar(g, MARGIN, MARGIN, meName, me, sync, myTurn);
        renderPlayerBar(g, sw - BAR_W - MARGIN, MARGIN, foeName, foe, sync, !myTurn);

        String turn = Component.translatable("cardduel.hud.turn", sync.turnNumber()).getString();
        g.drawCenteredString(mc.font, turn, sw / 2, MARGIN + 4, 0xFFFFFFFF);

        HudClickManager.renderEndTurnButton(g, myTurn);
    }

    private static void renderPlayerBar(GuiGraphics g, int x, int y, String name,
                                        ClientboundDuelSyncPayload.PlayerSyncView view,
                                        ClientboundDuelSyncPayload sync, boolean active) {
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
    }
}
