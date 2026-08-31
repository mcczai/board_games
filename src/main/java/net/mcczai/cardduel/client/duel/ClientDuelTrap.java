package net.mcczai.cardduel.client.duel;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 客户端缓存本人的秘密区（陷阱）内容（由 ClientboundDuelTrapPayload 写入）。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientDuelTrap {

    private static List<ItemStack> traps = List.of();

    private ClientDuelTrap() {
    }

    public static void update(List<ItemStack> traps) {
        ClientDuelTrap.traps = traps;
    }

    public static List<ItemStack> get() {
        return traps;
    }

    public static void clear() {
        traps = List.of();
    }
}
