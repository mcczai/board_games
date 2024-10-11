package net.mcczai.cardduel.items;

import com.google.common.collect.Maps;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class CardItemManager {
    public static final Map<String, DeferredHolder<AbstractCardItem,CardItem>> CARD_ITEM_MAP = Maps.newHashMap();
//TODO: 这里因为neoforge注册变动出问题了
    public static void registerCardItem(String name, DeferredHolder<AbstractCardItem,CardItem> registryObject){
        CARD_ITEM_MAP.put(name, registryObject);
    }

    public static DeferredHolder<AbstractCardItem,CardItem> getCardItemRegistryObject(String key){
        return CARD_ITEM_MAP.get(key);
    }

    public static Collection<DeferredHolder<AbstractCardItem,CardItem>> getAllCardItems(){
        return CARD_ITEM_MAP.values();
    }
}
