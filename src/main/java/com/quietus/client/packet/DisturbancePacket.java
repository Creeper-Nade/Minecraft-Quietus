package com.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.quietus.Quietus.MODID;

/** Server-authoritative world Disturbance data used by the Void Orrery display. */
public record DisturbancePacket(double disturbance, int stage, double volatility) implements CustomPacketPayload {
    public static final Type<DisturbancePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MODID, "disturbance"));

    public static final StreamCodec<FriendlyByteBuf, DisturbancePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeDouble(packet.disturbance());
                buffer.writeVarInt(packet.stage());
                buffer.writeDouble(packet.volatility());
            },
            buffer -> new DisturbancePacket(buffer.readDouble(), buffer.readVarInt(), buffer.readDouble())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
