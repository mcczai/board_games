package net.mcczai.cardduel.init;

import com.mojang.serialization.Codec;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.items.component.CardDataComponent;
import net.mcczai.cardduel.items.inventory.CardBagContents;
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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardBagContents>> CARD_BAG = DATA_COMPONENTS.registerComponentType(
            "card_bag",
            listBuilder -> listBuilder.persistent(CardBagContents.CODEC).networkSynchronized(CardBagContents.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IN_DUEL = DATA_COMPONENTS.registerComponentType(
            "in_duel",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

}
