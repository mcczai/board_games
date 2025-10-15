package net.mcczai.cardduel.init;

import net.mcczai.cardduel.API.item.CardItemManger;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.item.CardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CardduelMod.MODID);

    public static RegistryObject<CardItem> CARD_ITEM = ITEMS.register("card", () -> new CardItem(new Item.Properties()));

    public static RegistryObject<Item> DUELTABLE_BLOCK_ITEM = ITEMS.register("dueltable", () ->
            new BlockItem(
                        ModBlocks.DUELTABLE_BLOCK.get(), new Item.Properties().stacksTo(1)));

    @SubscribeEvent
    public static void onItemRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.ITEMS.getRegistryKey())) {
            CardItemManger.registerCardItem(CardItem.CARD_NAME, CARD_ITEM);
        }
    }
}
