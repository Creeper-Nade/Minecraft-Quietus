package com.quietus.client.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

import java.util.HashMap;
import java.util.Map;

public record SkillPacket(
    Map<Identifier, Double> skills
) implements CustomPacketPayload {

    public static final Type<SkillPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID,"skills_map"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.DOUBLE), SkillPacket::skills,
        SkillPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
