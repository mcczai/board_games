package net.mcczai.cardduel;


import net.mcczai.cardduel.API.resource.ResourceManager;
import net.mcczai.cardduel.config.CommonConfig;
import net.mcczai.cardduel.init.ModBlockEntities;
import net.mcczai.cardduel.init.ModBlocks;
import net.mcczai.cardduel.init.ModItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CardduelMod.MODID)
public class CardduelMod {

    public static final String MODID = "cardduel";
    public static final String DEFAULT_PACK = "default_card_pack";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CardduelMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.init());

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItem.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        CardduelCreativeTab.CREATIVE_MODE_TABS.register(bus);

        registerDefaultExtraCardPack();
    }

    public static void registerDefaultExtraCardPack(){
        String jarDefaultPackPath = String.format("/assets/%s/custom/%s",MODID,DEFAULT_PACK);
        ResourceManager.registerExtraGunPack(CardduelMod.class,jarDefaultPackPath);
    }
}
