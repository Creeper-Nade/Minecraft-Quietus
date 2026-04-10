package com.minecraftquietus.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public record GrapplingActiveHookPacket(boolean active,int hookEntityId)implements CustomPacketPayload {
    public static final Type<GrapplingActiveHookPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"hook_pack"));

    public GrapplingActiveHookPacket(final FriendlyByteBuf buf){
        this(buf.readBoolean(),buf.readInt());
    }
    public void Encode(FriendlyByteBuf buf){
        buf.writeBoolean(active());
        buf.writeInt(hookEntityId());
    }


    public static final StreamCodec<FriendlyByteBuf, GrapplingActiveHookPacket> STREAM_CODEC = StreamCodec.ofMember(GrapplingActiveHookPacket::Encode, GrapplingActiveHookPacket::new);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
