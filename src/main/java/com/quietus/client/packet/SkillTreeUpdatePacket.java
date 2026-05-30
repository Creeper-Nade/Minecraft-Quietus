package com.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.quietus.skilltree.SkillCategory;
import com.quietus.skilltree.SkillPointProgress;

public record SkillTreeUpdatePacket(
    Map<Identifier, SkillCategory> skillTree, 
    Map<Identifier, SkillPointProgress.ClientData> progresses
) implements CustomPacketPayload {
    
    public static final Type<SkillTreeUpdatePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID,"skill_tree_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeUpdatePacket> STREAM_CODEC = StreamCodec.composite(    
        ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, SkillCategory.STREAM_CODEC), SkillTreeUpdatePacket::skillTree,
        ByteBufCodecs.map(LinkedHashMap::new, Identifier.STREAM_CODEC, SkillPointProgress.ClientData.STREAM_CODEC), SkillTreeUpdatePacket::progresses,
        SkillTreeUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
