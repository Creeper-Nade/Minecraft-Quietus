package com.minecraftquietus.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPointProgress;

public record SkillTreeUpdatePacket(
    Map<ResourceLocation, SkillCategory> skillTree, 
    Map<ResourceLocation, SkillPointProgress.ClientData> progresses
) implements CustomPacketPayload {
    public static final Type<SkillTreeUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"skill_tree_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeUpdatePacket> STREAM_CODEC = StreamCodec.composite(    
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, SkillCategory.STREAM_CODEC), SkillTreeUpdatePacket::skillTree,
        ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, SkillPointProgress.ClientData.STREAM_CODEC), SkillTreeUpdatePacket::progresses,
        SkillTreeUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
