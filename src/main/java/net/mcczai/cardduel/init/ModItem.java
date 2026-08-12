package net.mcczai.cardduel.init;

import net.mcczai.cardduel.items.CardBagItem;
import net.mcczai.cardduel.items.CardItem;
import net.mcczai.cardduel.items.CardItemManager;
import net.mcczai.cardduel.resources.pojo.CardDataPOJO;
import net.mcczai.cardduel.resources.pojo.CardIndexPOJO;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static net.mcczai.cardduel.CardduelMod.MODID;

public class ModItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<CardItem> CARD_ITEM = ITEMS.register("card", ()-> new CardItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<CardBagItem> CARD_BAG_ITEM = ITEMS.register("card_bag",()->
            new CardBagItem(
                    new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DUELTABLE_BLOCK_ITEM = ITEMS.register("dueltable_block_item",()->
            new BlockItem(
                    ModBlocks.DUELTABLE_BLOCK.get(),new Item.Properties().stacksTo(1)));

    //@SubscribeEvent
    public static void onItemRegister(@NotNull RegisterEvent event) {
        System.out.println("EVENT RUN!!!");
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            System.out.println("CARD_ITEM in put!");
            // TODO: 在注册物品时调用，实现对不同类型的基础物品进行分类组成map，方便注册。但现在问题是写不进去,疑似订阅的事件有问题 2024.10.11 mcczai留
            // TODO: 此方法暂时弃用  2024.10.12 mcczai留
            CardItemManager.registerCardItem("tarp", CARD_ITEM);
            System.out.println("CARD_ITEM in put!");
        }
    }
}
