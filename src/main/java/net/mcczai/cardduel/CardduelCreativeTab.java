package net.mcczai.cardduel;

import net.mcczai.cardduel.API.item.CardTabType;
import net.mcczai.cardduel.init.ModItem;
import net.mcczai.cardduel.item.AbstractCardItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CardduelCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CardduelMod.MODID);

    public static RegistryObject<CreativeModeTab> CARDDUEL_TARP_TAB = CREATIVE_MODE_TABS.register("tarp_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cardduel.tarp"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItem.CARD_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItem.DUELTABLE_BLOCK_ITEM.get());
                output.acceptAll(AbstractCardItem.fillItemTab(CardTabType.TRAP));
            }).build()
    );

}
