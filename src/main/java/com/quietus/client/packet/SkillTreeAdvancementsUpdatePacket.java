package com.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

import java.util.HashSet;
import java.util.Set;

public record SkillTreeAdvancementsUpdatePacket(
    Set<Identifier> advancementIds
) implements CustomPacketPayload {
    
    public static final Type<SkillTreeAdvancementsUpdatePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID,"skill_tree_advancements_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeAdvancementsUpdatePacket> STREAM_CODEC = StreamCodec.composite(    
        ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC), SkillTreeAdvancementsUpdatePacket::advancementIds,
        SkillTreeAdvancementsUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
