package net.mcczai.cardduel.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mcczai.cardduel.API.CdAPI;
import net.mcczai.cardduel.API.item.nbt.CardDataAccessor;
import net.mcczai.cardduel.client.resource.ClientCardIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.Optional;

public class CardItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static CardItemRenderer INSTANCE;

    public static CardItemRenderer getInstance() {
        if (INSTANCE == null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                throw new IllegalStateException("CardItemRenderer accessed before Minecraft is initialized");
            }
            INSTANCE = new CardItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return INSTANCE;
    }

    private CardItemRenderer(BlockEntityRenderDispatcher dispatcher, net.minecraft.client.model.geom.EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof CardDataAccessor cardAccessor)) {
            return;
        }

        ResourceLocation cardId = cardAccessor.getCardId(stack);
        Optional<ClientCardIndex> cardIndex = CdAPI.getClientCardIndex(cardId);

        ResourceLocation texture = cardIndex.map(ClientCardIndex::getTexture).orElse(null);
        if (texture == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(-0.5f, -0.5f, 0);

        Matrix4f pose = poseStack.last().pose();
        RenderType renderType = RenderType.entityCutout(texture);
        var consumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, false, stack.hasFoil());

        float c = 1f / 16f;
        consumer.addVertex(pose, 0, 0, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(pose, 16 * c, 0, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(pose, 16 * c, 16 * c, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(pose, 0, 16 * c, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

        poseStack.popPose();
    }
}
