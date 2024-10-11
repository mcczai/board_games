package net.mcczai.cardduel.init;

import net.mcczai.cardduel.items.AbstractCardItem;
import net.mcczai.cardduel.items.CardBundleItem;
import net.mcczai.cardduel.items.CardItem;
import net.mcczai.cardduel.items.CardItemManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static net.mcczai.cardduel.CardduelMod.MODID;

public class ModItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final Supplier<CardItem> CARD_ITEM = ITEMS.register("card", ()-> new CardItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<CardBundleItem> CARD_BUNDLE_ITEM = ITEMS.register("card_bundle",()->
            new CardBundleItem(
                    new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DUELTABLE_BLOCK_ITEM = ITEMS.register("dueltable_block_item",()->
            new BlockItem(
                    ModBlocks.DUELTABLE_BLOCK.get(),new Item.Properties().stacksTo(1)));

    @SubscribeEvent
    public static void onItemRegister(@NotNull RegisterEvent event){
        if (event.getRegistryKey().equals(Registries.ITEM.registryKey())){
            // TODO: 这里的值记得改
            CardItemManager.registerCardItem("trap", (DeferredHolder<AbstractCardItem, CardItem>) CARD_ITEM);
        }
    }

}
