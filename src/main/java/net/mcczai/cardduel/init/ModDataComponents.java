package net.mcczai.cardduel.init;

import com.mojang.serialization.Codec;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.items.inventory.CardBundleContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(CardduelMod.MODID);

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> HP = DATA_COMPONENTS.registerComponentType(
            "hp",builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> ATK = DATA_COMPONENTS.registerComponentType(
            "atk",builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> MP = DATA_COMPONENTS.registerComponentType(
            "mp",builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> DESCRIPTION = DATA_COMPONENTS.registerComponentType(
            "description",builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> TYPE = DATA_COMPONENTS.registerComponentType(
            "type",builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> SKILL = DATA_COMPONENTS.registerComponentType(
            "skill",builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<ResourceLocation>> CARD_ID = DATA_COMPONENTS.registerComponentType(
            "card_id",builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ByteBufCodecs.STRING_UTF8.map(ResourceLocation::parse, ResourceLocation::toString))
    );

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<CardBundleContents>> CARD_BUNDLE = DATA_COMPONENTS.registerComponentType(
            "card_bundle",listBuilder -> listBuilder.persistent(CardBundleContents.CODEC).networkSynchronized(CardBundleContents.STREAM_CODEC)
    );


}
