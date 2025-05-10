package com.minecraftquietus.quietus.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public record ManaPack(int MaxMana, int Mana) implements CustomPacketPayload {
    public static final Type<ManaPack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"mana_pack"));

    public ManaPack(final FriendlyByteBuf buf){
        this(buf.readInt(),buf.readInt());
    }
    public void Encode(FriendlyByteBuf buf){
        buf.writeInt(MaxMana());
        buf.writeInt(Mana());
    }


    public static final StreamCodec<FriendlyByteBuf, ManaPack> MANA_PACK_STREAM_CODEC = StreamCodec.ofMember(ManaPack::Encode, ManaPack::new);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
