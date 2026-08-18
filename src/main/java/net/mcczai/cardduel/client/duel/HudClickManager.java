package net.mcczai.cardduel.client.duel;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.mcczai.cardduel.network.payload.ServerboundEndTurnPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 无 Screen 的 HUD 点击命中层：
 * P1-2 处理"结束回合"按钮；P1-3/P1-4 的手牌点选、攻击目标点选也走这里。
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class HudClickManager {

    private static final int BTN_W = 80;
    private static final int BTN_H = 20;
    private static final int BTN_MARGIN = 8;

    /**
     * "结束回合"按钮区域（右下角，手牌区右侧）。
     */
    public static Rect2i endTurnButtonRect(int screenW, int screenH) {
        return new Rect2i(screenW - BTN_W - BTN_MARGIN, screenH - BTN_H - BTN_MARGIN, BTN_W, BTN_H);
    }

    public static void renderEndTurnButton(GuiGraphics g, boolean enabled) {
        Minecraft mc = Minecraft.getInstance();
        Rect2i rect = endTurnButtonRect(g.guiWidth(), g.guiHeight());
        int color = enabled ? 0xFF43A047 : 0xFF616161;
        g.fill(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color);
        g.drawCenteredString(mc.font, Component.translatable("cardduel.hud.end_turn"),
                rect.getX() + rect.getWidth() / 2, rect.getY() + (BTN_H - 8) / 2, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != 0 || event.getAction() != 1) {
            return; // 仅处理鼠标左键按下
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        ClientboundDuelSyncPayload sync = ClientDuelState.get();
        if (sync == null || !DuelCameraManager.isActivePhase(sync.phase())) {
            return;
        }
        if (!mc.player.getUUID().equals(sync.activeUuid())) {
            return; // 不是自己的回合
        }

        double mx = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        Rect2i rect = endTurnButtonRect(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        if (rect.contains((int) mx, (int) my)) {
            PacketDistributor.sendToServer(new ServerboundEndTurnPayload());
            event.setCanceled(true);
        }
    }
}
