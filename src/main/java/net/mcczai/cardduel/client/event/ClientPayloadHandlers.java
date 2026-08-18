package net.mcczai.cardduel.client.event;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.client.gui.screens.duel.DuelSetupScreen;
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
                        ClientPayloadHandlers::handleOpenSetup);
    }

    private static void handleOpenSetup(ClientboundOpenSetupPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new DuelSetupScreen(payload.tablePos())));
    }
}
