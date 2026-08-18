package net.mcczai.cardduel.resources;

import net.mcczai.cardduel.CardduelMod;
import net.minecraft.resources.ResourceLocation;

public final class DefaultAssets {
    public static final ResourceLocation EMPTY_CARD_ID = ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID,"empty");
    public static final ResourceLocation DEFAULT_CARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID,"default_card");
    /** 后手硬币卡（P1-4）：使用后本回合 +1 法力并消失 */
    public static final ResourceLocation COIN_CARD_ID = ResourceLocation.fromNamespaceAndPath(CardduelMod.MODID,"coin");
}
