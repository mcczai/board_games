package net.mcczai.cardduel.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mcczai.cardduel.API.CdAPI;
import net.mcczai.cardduel.API.item.nbt.CardDataAccessor;
import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.client.resource.ClientCardIndex;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DuelTableBlockEntityRenderer implements BlockEntityRenderer<DuelTableBlockEntity> {
    private static final float CARD_W = 10f / 16f;
    private static final float CARD_H = 14f / 16f;
    private static final float GAP = 1f / 16f;
    private static final int COLUMNS = 9;
    private static final int ROWS = 3;

    public DuelTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DuelTableBlockEntity table, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        List<ItemStack> deck = table.getDeck();
        if (deck.isEmpty()) {
            return;
        }
        float startX = (1f - (COLUMNS * CARD_W + (COLUMNS - 1) * GAP)) / 2f;
        float startZ = (1f - (ROWS * CARD_H + (ROWS - 1) * GAP)) / 2f;
        poseStack.pushPose();
        poseStack.translate(0, 0.002f, 0);
        for (int i = 0; i < deck.size() && i < COLUMNS * ROWS; i++) {
            ItemStack card = deck.get(i);
            if (!(card.getItem() instanceof CardDataAccessor accessor)) {
                continue;
            }
            ResourceLocation cardId = accessor.getCardId(card);
            Optional<ClientCardIndex> cardIndex = CdAPI.getClientCardIndex(cardId);
            if (cardIndex.isEmpty()) {
                continue;
            }
            ResourceLocation texture = cardIndex.get().getTexture();
            if (texture == null) {
                continue;
            }
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            poseStack.pushPose();
            poseStack.translate(startX + col * (CARD_W + GAP), 0, startZ + row * (CARD_H + GAP));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            renderCardQuad(poseStack, buffer, texture, packedLight, packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderCardQuad(PoseStack poseStack, MultiBufferSource buffer,
                                       ResourceLocation texture, int packedLight, int packedOverlay) {
        Matrix4f pose = poseStack.last().pose();
        RenderType renderType = RenderType.entityCutout(texture);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, false, false);
        consumer.addVertex(pose, 0, 0, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(pose, CARD_W, 0, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(pose, CARD_W, CARD_H, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(pose, 0, CARD_H, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
    }
}
