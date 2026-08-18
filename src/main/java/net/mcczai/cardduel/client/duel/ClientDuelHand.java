package net.mcczai.cardduel.client.duel;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 客户端缓存本人的手牌内容（由 ClientboundDuelHandPayload 写入）。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientDuelHand {

    private static List<ItemStack> hand = List.of();

    private ClientDuelHand() {
    }

    public static void update(List<ItemStack> hand) {
        ClientDuelHand.hand = hand;
    }

    public static List<ItemStack> get() {
        return hand;
    }

    public static void clear() {
        hand = List.of();
    }
}
