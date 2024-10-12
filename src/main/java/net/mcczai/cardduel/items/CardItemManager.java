package net.mcczai.cardduel.items;

import com.google.common.collect.Maps;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Collection;
import java.util.Map;

public class CardItemManager {
    public static final Map<String, DeferredItem<? extends AbstractCardItem>> CARD_ITEM_MAP = Maps.newHashMap();
//TODO: CARD_ITEM_MAP是空的！
    public static void registerCardItem(String TypeName, DeferredItem<? extends AbstractCardItem> registryObject){
        System.out.println("CARD_ITEM in put!");
        CARD_ITEM_MAP.put(TypeName, registryObject);
    }

    public static DeferredItem<? extends AbstractCardItem> getCardItemRegistryObject(String key){
        System.out.println("CARD_ITEM GET!!");
        System.out.println(CARD_ITEM_MAP);
        return CARD_ITEM_MAP.get(key);
    }

    public static Collection<DeferredItem<? extends AbstractCardItem>> getAllCardItems(){
        return CARD_ITEM_MAP.values();
    }
}
