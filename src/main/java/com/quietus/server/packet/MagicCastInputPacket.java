package com.quietus.server.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.quietus.Quietus.MODID;

public record MagicCastInputPacket(int action, float progress) implements CustomPacketPayload {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CANCEL = 2;

    public static final Type<MagicCastInputPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MODID, "magic_cast_input"));

    public static final StreamCodec<ByteBuf, MagicCastInputPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MagicCastInputPacket::action,
            ByteBufCodecs.FLOAT, MagicCastInputPacket::progress,
            MagicCastInputPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
