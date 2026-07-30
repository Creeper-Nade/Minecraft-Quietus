package com.quietus.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.quietus.Quietus.MODID;

public record GrapplingHookActionPacket() implements CustomPacketPayload {
    public static final Type<GrapplingHookActionPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MODID, "grappling_hook_action"));

    public static final StreamCodec<FriendlyByteBuf, GrapplingHookActionPacket> STREAM_CODEC =
            StreamCodec.unit(new GrapplingHookActionPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
