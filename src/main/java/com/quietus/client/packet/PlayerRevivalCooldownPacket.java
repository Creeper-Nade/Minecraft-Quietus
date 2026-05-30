package com.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

public record PlayerRevivalCooldownPacket (int cooldown) implements CustomPacketPayload  {
    
    public static final CustomPacketPayload.Type<PlayerRevivalCooldownPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID,"revive_cd"));

    public static final StreamCodec<FriendlyByteBuf,  PlayerRevivalCooldownPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayerRevivalCooldownPacket::cooldown,
            PlayerRevivalCooldownPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
