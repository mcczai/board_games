package net.mcczai.cardduel.client.event;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.client.duel.ClientDuelHand;
import net.mcczai.cardduel.client.duel.ClientDuelState;
import net.mcczai.cardduel.client.gui.screens.duel.DuelSetupScreen;
import net.mcczai.cardduel.network.payload.ClientboundDuelHandPayload;
import net.mcczai.cardduel.network.payload.ClientboundDuelSyncPayload;
import net.mcczai.cardduel.network.payload.ClientboundOpenSetupPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端网络包注册与处理。
 */
@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientPayloadHandlers {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(ClientboundOpenSetupPayload.TYPE, ClientboundOpenSetupPayload.STREAM_CODEC,
                        ClientPayloadHandlers::handleOpenSetup)
                .playToClient(ClientboundDuelSyncPayload.TYPE, ClientboundDuelSyncPayload.STREAM_CODEC,
                        ClientPayloadHandlers::handleDuelSync)
                .playToClient(ClientboundDuelHandPayload.TYPE, ClientboundDuelHandPayload.STREAM_CODEC,
                        ClientPayloadHandlers::handleDuelHand);
    }

    private static void handleOpenSetup(ClientboundOpenSetupPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new DuelSetupScreen(payload.tablePos())));
    }

    private static void handleDuelSync(ClientboundDuelSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientDuelState.update(payload));
    }

    private static void handleDuelHand(ClientboundDuelHandPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientDuelHand.update(payload.hand()));
    }
}
