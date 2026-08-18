package net.mcczai.cardduel.client.duel;

import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端缓存最近一次对局同步状态（P1-2 HUD 读取用）。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientDuelState {

    @Nullable
    private static ClientboundDuelSyncPayload latest;

    private ClientDuelState() {
    }

    public static void update(ClientboundDuelSyncPayload payload) {
        latest = payload;
    }

    @Nullable
    public static ClientboundDuelSyncPayload get() {
        return latest;
    }

    public static void clear() {
        latest = null;
    }
}
