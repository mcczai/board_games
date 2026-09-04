package net.mcczai.cardduel.client.event;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.client.gui.screens.inventory.CardBagScreen;
import net.mcczai.cardduel.client.renderer.blockentity.DuelTableBlockEntityRenderer;
import net.mcczai.cardduel.client.resource.ClientReloadManager;
import net.mcczai.cardduel.init.ModBlockEntities;
import net.mcczai.cardduel.init.ModMenuType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CardduelMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(ModBlockEntities.DUELTABLE.get(), DuelTableBlockEntityRenderer::new);
        ClientReloadManager.reloadAllPack();
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuType.CARD_BAG_MENU.get(), CardBagScreen::new);
    }
}
