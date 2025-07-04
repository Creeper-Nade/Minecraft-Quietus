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

public record GhostStatePayload(boolean isGhost, Component message,int Max_CD) implements CustomPacketPayload {
    public static final Type<GhostStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"ghost_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            GhostStatePayload::isGhost,
            ComponentSerialization.TRUSTED_STREAM_CODEC,
            GhostStatePayload::message,
            ByteBufCodecs.INT,
            GhostStatePayload::Max_CD,
            GhostStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
