package com.minecraftquietus.quietus.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public record GhostStatePacket(boolean isGhost, Component message,int Max_CD,boolean hardcore) implements CustomPacketPayload {
    public static final Type<GhostStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"ghost_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            GhostStatePacket::isGhost,
            ComponentSerialization.TRUSTED_STREAM_CODEC,
            GhostStatePacket::message,
            ByteBufCodecs.INT,
            GhostStatePacket::Max_CD,
            ByteBufCodecs.BOOL,
            GhostStatePacket::hardcore,
            GhostStatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
