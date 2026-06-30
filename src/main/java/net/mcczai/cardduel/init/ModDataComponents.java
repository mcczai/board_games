package net.mcczai.cardduel.init;

import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.items.component.CardDataComponent;
import net.mcczai.cardduel.items.inventory.CardBundleContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(CardduelMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardDataComponent>> CARD_DATA = DATA_COMPONENTS.registerComponentType(
            "card_data",
            builder -> builder.persistent(CardDataComponent.CODEC).networkSynchronized(CardDataComponent.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> CARD_ID = DATA_COMPONENTS.registerComponentType(
            "card_id",
            builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ByteBufCodecs.STRING_UTF8.map(ResourceLocation::parse, ResourceLocation::toString))
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardBundleContents>> CARD_BUNDLE = DATA_COMPONENTS.registerComponentType(
            "card_bundle",
            listBuilder -> listBuilder.persistent(CardBundleContents.CODEC).networkSynchronized(CardBundleContents.STREAM_CODEC)
    );

}
