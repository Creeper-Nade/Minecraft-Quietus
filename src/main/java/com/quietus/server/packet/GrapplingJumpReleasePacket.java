package com.quietus.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.quietus.Quietus.MODID;

public record GrapplingJumpReleasePacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GrapplingJumpReleasePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID,"grappling_hook_jump_reset"));

    public static final StreamCodec<FriendlyByteBuf, GrapplingJumpReleasePacket> STREAM_CODEC = StreamCodec.unit(new GrapplingJumpReleasePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


}