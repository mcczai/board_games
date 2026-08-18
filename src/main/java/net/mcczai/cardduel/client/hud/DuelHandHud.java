package net.mcczai.cardduel.client.hud;

import net.mcczai.cardduel.API.CdAPI;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.API.item.nbt.CardDataAccessor;
import net.mcczai.cardduel.client.duel.ClientDuelHand;
import net.mcczai.cardduel.client.duel.ClientDuelState;
import net.mcczai.cardduel.client.duel.DuelCameraManager;
import net.mcczai.cardduel.client.resource.ClientCardIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 炉石式手牌 HUD：对局中把手牌（数据源 = ClientboundDuelHandPayload）平铺在窗口下方。
 * 手牌点击选牌/出牌交互在 P1-3 接入（HudClickManager）。
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class DuelHandHud {
    private static final int CARD_W = 44;
    private static final int CARD_H = 62;
    private static final int GAP = 4;
    private static final int HOVER_SCALE = 4;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || mc.level == null) {
            return;
        }
        if (!DuelCameraManager.inDuelView()) {
            return;
        }
        List<ItemStack> hand = ClientDuelHand.get();
        if (hand.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        List<ResourceLocation> textures = new ArrayList<>();
        for (ItemStack stack : hand) {
            if (!(stack.getItem() instanceof CardDataAccessor accessor)) {
                continue;
            }
            Optional<ClientCardIndex> cardIndex = CdAPI.getClientCardIndex(accessor.getCardId(stack));
            ResourceLocation texture = cardIndex.map(ClientCardIndex::getTexture).orElse(null);
            if (texture != null) {
                textures.add(texture);
            }
        }
        if (textures.isEmpty()) {
            return;
        }

        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight();

        int totalWidth = textures.size() * CARD_W + (textures.size() - 1) * GAP;
        int startX = (screenWidth - totalWidth) / 2;
        int y = screenHeight - 78;

        int hovered = -1;
        for (int i = 0; i < textures.size(); i++) {
            int x = startX + i * (CARD_W + GAP);
            if (mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H) {
                hovered = i;
            }
        }

        for (int i = 0; i < textures.size(); i++) {
            if (i == hovered) {
                continue;
            }
            drawCard(guiGraphics, textures.get(i), startX + i * (CARD_W + GAP), y, CARD_W, CARD_H, false);
        }
        if (hovered >= 0) {
            int hx = startX + hovered * (CARD_W + GAP) - HOVER_SCALE;
            int hy = y - HOVER_SCALE;
            drawCard(guiGraphics, textures.get(hovered), hx, hy, CARD_W + HOVER_SCALE * 2, CARD_H + HOVER_SCALE * 2, true);
        }
    }

    private static void drawCard(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, boolean selected) {
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, selected ? 0xFFFFFFFF : 0xFF3A3A3A);
        guiGraphics.blit(texture, x, y, width, height, 0, 0, width, height, width, height);
        if (!selected) {
            guiGraphics.fill(x, y, x + width, y + height, 0x66000000);
        }
    }
}
