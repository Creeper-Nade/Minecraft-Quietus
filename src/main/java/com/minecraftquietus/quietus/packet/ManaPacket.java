package com.minecraftquietus.quietus.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public record ManaPacket(int MaxMana, int Mana,boolean FastCharging) implements CustomPacketPayload {
    public static final Type<ManaPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"mana_pack"));

    public ManaPacket(final FriendlyByteBuf buf){
        this(buf.readInt(),buf.readInt(),buf.readBoolean());
    }
    public void Encode(FriendlyByteBuf buf){
        buf.writeInt(MaxMana());
        buf.writeInt(Mana());
        buf.writeBoolean(FastCharging());
    }


    public static final StreamCodec<FriendlyByteBuf, ManaPacket> STREAM_CODEC = StreamCodec.ofMember(ManaPacket::Encode, ManaPacket::new);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
