package net.mcczai.cardduel.event;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.resources.DedicatedServerReloadManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CardduelMod.MODID)
public class CommonLoadPack {

    @SubscribeEvent
    public static void loadCardPack(FMLCommonSetupEvent commonSetupEvent){
        DedicatedServerReloadManager.loadCardPack();
    }
}
