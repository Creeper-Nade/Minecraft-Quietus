package com.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ItemContainerContents;

import static com.quietus.Quietus.MODID;

/**
 * Packet to client to decay certain items in an Entity.
 * For all LivingEntities will decay the item in EquipmentSlot as provided.
 * For ItemFrame decays its item regardless of EquipmentSlot.
 * See {@link ClientPayloadHandler}
 */
public record WeatherItemContainerPacket (
    int entityId,
    EquipmentSlot slot,
    ItemContainerContents containerContents
) implements CustomPacketPayload  {
    
    public static final CustomPacketPayload.Type<WeatherItemContainerPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID,"weathering_sync_item_container"));

    @SuppressWarnings("unchecked")
    public static final StreamCodec<FriendlyByteBuf, WeatherItemContainerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            WeatherItemContainerPacket::entityId,
            EquipmentSlot.STREAM_CODEC,
            WeatherItemContainerPacket::slot,
            (StreamCodec<FriendlyByteBuf, ItemContainerContents>) (StreamCodec<?, ?>) ItemContainerContents.STREAM_CODEC,
            WeatherItemContainerPacket::containerContents,
            WeatherItemContainerPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
