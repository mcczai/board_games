package net.mcczai.cardduel.API.item;

import com.google.common.collect.Maps;
import net.mcczai.cardduel.item.AbstractCardItem;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class CardItemManger {
    public static final Map<String, RegistryObject<? extends AbstractCardItem>> CARD_ITEM_MAP = Maps.newHashMap();

    public static void registerCardItem(String name, RegistryObject<? extends AbstractCardItem> item) {
        CARD_ITEM_MAP.put(name, item);
    }
}
