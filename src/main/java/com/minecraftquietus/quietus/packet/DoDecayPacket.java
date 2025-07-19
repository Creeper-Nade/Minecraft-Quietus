package com.minecraftquietus.quietus.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import static com.minecraftquietus.quietus.Quietus.MODID;

/**
 * Packet to client to decay certain items in an Entity.
 * For all LivingEntities will decay the item in EquipmentSlot as provided.
 * For ItemFrame decays its item regardless of EquipmentSlot.
 * See {@link ClientPayloadHandler}
 */
public record DoDecayPacket (
    int entityId,
    EquipmentSlot slot,
    int amount
) implements CustomPacketPayload  {
    
    public static final CustomPacketPayload.Type<DoDecayPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"weathering_sync_decay"));

    public static final StreamCodec<FriendlyByteBuf,  DoDecayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DoDecayPacket::entityId,
            EquipmentSlot.STREAM_CODEC,
            DoDecayPacket::slot,
            ByteBufCodecs.INT,
            DoDecayPacket::amount,
            DoDecayPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
