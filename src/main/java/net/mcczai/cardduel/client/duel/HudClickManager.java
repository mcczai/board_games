package net.mcczai.cardduel.client.duel;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.client.hud.DuelHandHud;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.mcczai.cardduel.network.payload.ServerboundAttackPayload;
import net.mcczai.cardduel.network.payload.ServerboundEndTurnPayload;
import net.mcczai.cardduel.network.payload.ServerboundMulliganPayload;
import net.mcczai.cardduel.network.payload.ServerboundPlayCardPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * 无 Screen 的 HUD 点击命中层：
 *  - 右下按钮：PLAYING = 结束回合；MULLIGAN = 确认换牌
 *  - 手牌点击：出牌选中 / 换牌选中
 *  - 桌面射线命中：己方空槽出牌、己方有卡槽选中攻击、对方目标攻击/打脸
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class HudClickManager {

    private static final int BTN_W = 80;
    private static final int BTN_H = 20;
    private static final int BTN_MARGIN = 8;

    /** 桌面战场槽位几何（与 DuelTableBlockEntityRenderer 保持一致） */
    private static final float BOARD_STEP = (2.6F + 0.3F) / 16F;

    public static Rect2i endTurnButtonRect(int screenW, int screenH) {
        return new Rect2i(screenW - BTN_W - BTN_MARGIN, screenH - BTN_H - BTN_MARGIN, BTN_W, BTN_H);
    }

    /**
     * 右下按钮：PLAYING 显示"结束回合"（非己方回合置灰）；MULLIGAN 显示"确认换牌"。
     */
    public static void renderEndTurnButton(GuiGraphics g, boolean myTurn, boolean mulliganPhase) {
        Minecraft mc = Minecraft.getInstance();
        Rect2i rect = endTurnButtonRect(g.guiWidth(), g.guiHeight());
        int color = mulliganPhase ? 0xFFF9A825 : (myTurn ? 0xFF43A047 : 0xFF616161);
        g.fill(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color);
        Component label = mulliganPhase
                ? Component.translatable("cardduel.hud.mulligan_confirm")
                : Component.translatable("cardduel.hud.end_turn");
        g.drawCenteredString(mc.font, label, rect.getX() + rect.getWidth() / 2, rect.getY() + (BTN_H - 8) / 2, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != 0 || event.getAction() != 1) {
            return; // 仅处理鼠标左键按下
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || mc.level == null) {
            return;
        }
        ClientboundDuelSyncPayload sync = ClientDuelState.get();
        if (sync == null || !DuelCameraManager.isActivePhase(sync.phase())) {
            return;
        }

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double mx = mc.mouseHandler.xpos() * sw / (double) mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * sh / (double) mc.getWindow().getScreenHeight();
        boolean myTurn = mc.player.getUUID().equals(sync.activeUuid());
        boolean mulligan = "MULLIGAN".equals(sync.phase());

        // 1. 右下按钮
        Rect2i btn = endTurnButtonRect(sw, sh);
        if (btn.contains((int) mx, (int) my)) {
            if (mulligan) {
                PacketDistributor.sendToServer(new ServerboundMulliganPayload(
                        new ArrayList<>(DuelInteraction.getMulliganSelection())));
                DuelInteraction.clearMulligan();
            } else if (myTurn) {
                PacketDistributor.sendToServer(new ServerboundEndTurnPayload());
            }
            event.setCanceled(true);
            return;
        }

        // 2. 手牌
        int handIndex = DuelHandHud.handIndexAt(mx, my, ClientDuelHand.get().size(), sw, sh);
        if (handIndex >= 0) {
            if (mulligan) {
                DuelInteraction.toggleMulligan(handIndex);
            } else if (myTurn) {
                DuelInteraction.toggleHand(handIndex);
            }
            event.setCanceled(true);
            return;
        }

        // 3. 桌面射线
        SlotHit hit = hitTable(mc, sync, mx, my);
        if (hit != null) {
            handleTableClick(mc, sync, hit, myTurn, mulligan);
            event.setCanceled(true);
            return;
        }

        // 4. 空白处：清空选中
        DuelInteraction.clear();
    }

    private static void handleTableClick(Minecraft mc, ClientboundDuelSyncPayload sync, SlotHit hit,
                                         boolean myTurn, boolean mulligan) {
        if (mulligan || !myTurn) {
            DuelInteraction.clear();
            return;
        }
        if (!(mc.level.getBlockEntity(sync.tablePos()) instanceof DuelTableBlockEntity table)) {
            return;
        }
        boolean isHost = mc.player.getUUID().equals(sync.hostUuid());
        DuelPlayerData myData = isHost ? table.getHostData() : table.getGuestData();
        DuelPlayerData foeData = isHost ? table.getGuestData() : table.getHostData();

        if (hit.hostHalf() == isHost) {
            // 己方半场
            if (myData.getBoard()[hit.slot()].isEmpty()) {
                if (DuelInteraction.getSelectedHand() >= 0) {
                    PacketDistributor.sendToServer(new ServerboundPlayCardPayload(
                            DuelInteraction.getSelectedHand(), hit.slot()));
                }
                DuelInteraction.clear();
            } else {
                DuelInteraction.toggleBoard(hit.slot());
            }
        } else {
            // 对方半场：有选中己方卡 → 攻击对方卡或打脸（点空处）
            if (DuelInteraction.getSelectedBoard() >= 0) {
                int target = foeData.getBoard()[hit.slot()].isEmpty() ? -1 : hit.slot();
                PacketDistributor.sendToServer(new ServerboundAttackPayload(
                        DuelInteraction.getSelectedBoard(), target));
            }
            DuelInteraction.clear();
        }
    }

    /**
     * 屏幕坐标 → 桌面平面（俯视相机透视精确映射）→ 半场与槽位。
     */
    @Nullable
    private static SlotHit hitTable(Minecraft mc, ClientboundDuelSyncPayload sync, double mouseX, double mouseY) {
        Direction facing = Direction.byName(sync.facing());
        if (facing == null) {
            return null;
        }
        int dirZ = facing == Direction.NORTH ? -1 : 1;
        BlockPos bePos = sync.tablePos();
        double centerX = bePos.getX() + 0.5;
        double centerZ = bePos.getZ() + 0.5 + dirZ * 0.5;
        double camY = bePos.getY() + 0.5 + DuelCameraManager.CAMERA_HEIGHT;
        double t = camY - (bePos.getY() + 1.0);

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double fov = mc.options.fov().get();
        double tanY = Math.tan(Math.toRadians(fov / 2.0));
        double aspect = (double) sw / sh;
        double ndcX = mouseX / sw * 2 - 1;
        double ndcY = mouseY / sh * 2 - 1;

        double worldX = centerX + ndcX * t * tanY * aspect;
        double worldZ = centerZ - ndcY * t * tanY;

        double localX = worldX - bePos.getX();
        double localZ = worldZ - bePos.getZ();

        double hostZ = 0.5 + dirZ * 0.25;
        double guestZ = 0.5 + dirZ * 0.75;
        boolean hostHalf = Math.abs(localZ - hostZ) < Math.abs(localZ - guestZ);
        double halfZ = hostHalf ? hostZ : guestZ;
        if (Math.abs(localZ - halfZ) > 0.25) {
            return null;
        }

        float start = 6 * BOARD_STEP / 2F;
        int slot = -1;
        for (int i = 0; i < DuelPlayerData.BOARD_SIZE; i++) {
            double cx = 0.5 + (i * BOARD_STEP - start);
            if (Math.abs(localX - cx) <= BOARD_STEP / 2) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            return null;
        }
        return new SlotHit(hostHalf, slot);
    }

    public record SlotHit(boolean hostHalf, int slot) {
    }
}
