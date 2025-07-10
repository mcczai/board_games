package net.mcczai.cardduel.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IMessage<T> {
    void encode(T message , FriendlyByteBuf byteBuf);

    T decode(FriendlyByteBuf byteBuf);

    void handle(T message);
}
