package net.mcczai.cardduel;

import net.mcczai.cardduel.API.resource.ResourceManager;
import net.mcczai.cardduel.config.CommonConfig;
import net.mcczai.cardduel.config.DuelConfig;
import net.mcczai.cardduel.init.*;
import net.mcczai.cardduel.network.DuelNet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CardduelMod.MODID)
public class CardduelMod {

    public static final String MODID = "cardduel";
    public static final String DEFAULT_PACK = "default_card_pack";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CardduelMod(IEventBus bus, ModContainer modContainer){

        modContainer.registerConfig(ModConfig.Type.COMMON,CommonConfig.init());
        modContainer.registerConfig(ModConfig.Type.SERVER, DuelConfig.init());

        ModBlocks.BLOCKS.register(bus);
        ModItem.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModDataComponents.DATA_COMPONENTS.register(bus);
        CardduelCreativeTab.CARDDUEL_TABS.register(bus);
        ModMenuType.MENUS.register(bus);
        ModAttachments.ATTACHMENT_TYPES.register(bus);

        bus.addListener(RegisterPayloadHandlersEvent.class, DuelNet::registerServer);

        registerDefaultExtraCardPack();
    }

    public static void registerDefaultExtraCardPack() {
        String jarDefaultPackPath = String.format("/assets/%s/custom/%s",MODID,DEFAULT_PACK);
        ResourceManager.registerExtraCardPack(CardduelMod.class,jarDefaultPackPath);
    }


}
