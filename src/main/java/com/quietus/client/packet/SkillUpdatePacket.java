package com.quietus.client.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

public record SkillUpdatePacket(
    Identifier skillId,
    double value // all cast into double
) implements CustomPacketPayload {

    public static final Type<SkillUpdatePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID,"skill_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, SkillUpdatePacket::skillId,
        ByteBufCodecs.DOUBLE, SkillUpdatePacket::value,
        SkillUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
