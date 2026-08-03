package com.quietus.client.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

public record MagicCastStartPacket(boolean accepted, long seed, int hand) implements CustomPacketPayload {
    public static final Type<MagicCastStartPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MODID, "magic_cast_start"));
    public static final StreamCodec<ByteBuf, MagicCastStartPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MagicCastStartPacket::accepted,
            ByteBufCodecs.VAR_LONG, MagicCastStartPacket::seed,
            ByteBufCodecs.VAR_INT, MagicCastStartPacket::hand,
            MagicCastStartPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
