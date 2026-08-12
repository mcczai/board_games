package net.mcczai.cardduel.init;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.items.inventory.CardBagMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuType {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CardduelMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CardBagMenu>> CARD_BAG_MENU =
            MENUS.register("card_bag_menu", ()-> new MenuType<>(CardBagMenu::new, FeatureFlags.VANILLA_SET));

}
