package net.mcczai.cardduel.client.duel;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 对局中的俯视相机与界面裁剪：
 *  - 相机锁定双桌中心正上方，完全垂直俯视（90°）
 *  - 隐藏第一人称手部与所有原版 HUD 层（快捷栏/经验条/血量/饥饿/准星等）
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class DuelCameraManager {

    /** 相机离桌面高度（米） */
    private static final float CAMERA_HEIGHT = 6.0F;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        ClientboundDuelSyncPayload sync = ClientDuelState.get();
        if (sync == null || !isActivePhase(sync.phase())) {
            return;
        }
        Direction facing = Direction.byName(sync.facing());
        if (facing == null) {
            return;
        }
        // 双桌中心 = 主桌中心 + 朝向方向半步（双桌沿 facing 排列）
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 center = sync.tablePos().getCenter().add(dir.scale(0.5D));

        Camera camera = event.getCamera();
        camera.setPosition(center.x, center.y + CAMERA_HEIGHT, center.z);
        camera.setRotation(0.0F, 90.0F, 0.0F);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (inDuelView()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (inDuelView() && isVanillaLayerToHide(event.getName())) {
            event.setCanceled(true);
        }
    }

    private static boolean isVanillaLayerToHide(ResourceLocation name) {
        return name.equals(VanillaGuiLayers.HOTBAR)
                || name.equals(VanillaGuiLayers.EXPERIENCE_BAR)
                || name.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)
                || name.equals(VanillaGuiLayers.PLAYER_HEALTH)
                || name.equals(VanillaGuiLayers.ARMOR_LEVEL)
                || name.equals(VanillaGuiLayers.FOOD_LEVEL)
                || name.equals(VanillaGuiLayers.AIR_LEVEL)
                || name.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)
                || name.equals(VanillaGuiLayers.CROSSHAIR)
                || name.equals(VanillaGuiLayers.JUMP_METER);
    }

    /**
     * 是否处于对局视角（PLAYING / MULLIGAN）。
     */
    public static boolean inDuelView() {
        ClientboundDuelSyncPayload sync = ClientDuelState.get();
        return sync != null && isActivePhase(sync.phase());
    }

    public static boolean isActivePhase(String phase) {
        return "PLAYING".equals(phase) || "MULLIGAN".equals(phase);
    }

    /**
     * 双桌中心（俯视相机与座位锚点共用）。
     */
    public static Vec3 tableCenter(ClientboundDuelSyncPayload sync) {
        Direction facing = Direction.byName(sync.facing());
        if (facing == null) {
            return sync.tablePos().getCenter();
        }
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal());
        return sync.tablePos().getCenter().add(dir.scale(0.5D));
    }

    public static BlockPos tablePos(ClientboundDuelSyncPayload sync) {
        return sync.tablePos();
    }
}
