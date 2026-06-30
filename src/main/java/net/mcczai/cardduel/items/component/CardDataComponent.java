package net.mcczai.cardduel.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CardDataComponent(int hp, int atk, int mp, String type, String skill, String tribe) {

    public static final CardDataComponent DEFAULT = new CardDataComponent(1, 1, 1, "trap", "0", "");

    public static final Codec<CardDataComponent> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            Codec.INT.fieldOf("hp").forGetter(CardDataComponent::hp),
            Codec.INT.fieldOf("atk").forGetter(CardDataComponent::atk),
            Codec.INT.fieldOf("mp").forGetter(CardDataComponent::mp),
            Codec.STRING.fieldOf("type").forGetter(CardDataComponent::type),
            Codec.STRING.fieldOf("skill").forGetter(CardDataComponent::skill),
            Codec.STRING.optionalFieldOf("tribe", "").forGetter(CardDataComponent::tribe)
    ).apply(ins, CardDataComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CardDataComponent::hp,
            ByteBufCodecs.INT, CardDataComponent::atk,
            ByteBufCodecs.INT, CardDataComponent::mp,
            ByteBufCodecs.STRING_UTF8, CardDataComponent::type,
            ByteBufCodecs.STRING_UTF8, CardDataComponent::skill,
            ByteBufCodecs.STRING_UTF8, CardDataComponent::tribe,
            CardDataComponent::new
    );

    public CardDataComponent withHp(int hp) {
        return new CardDataComponent(Math.max(hp, 1), atk, mp, type, skill, tribe);
    }

    public CardDataComponent withAtk(int atk) {
        return new CardDataComponent(hp, Math.max(atk, 1), mp, type, skill, tribe);
    }

    public CardDataComponent withMp(int mp) {
        return new CardDataComponent(hp, atk, Math.max(mp, 1), type, skill, tribe);
    }

    public CardDataComponent withType(String type) {
        return new CardDataComponent(hp, atk, mp, type, skill, tribe);
    }

    public CardDataComponent withSkill(String skill) {
        return new CardDataComponent(hp, atk, mp, type, skill, tribe);
    }

    public CardDataComponent withTribe(String tribe) {
        return new CardDataComponent(hp, atk, mp, type, skill, tribe);
    }
}
