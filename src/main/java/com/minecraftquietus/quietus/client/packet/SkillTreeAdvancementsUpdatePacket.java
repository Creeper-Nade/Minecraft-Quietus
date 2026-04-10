package com.minecraftquietus.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPointProgress;

public record SkillTreeAdvancementsUpdatePacket(
    Set<ResourceLocation> advancementIds
) implements CustomPacketPayload {
    
    public static final Type<SkillTreeAdvancementsUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"skill_tree_advancements_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeAdvancementsUpdatePacket> STREAM_CODEC = StreamCodec.composite(    
        ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC), SkillTreeAdvancementsUpdatePacket::advancementIds,
        SkillTreeAdvancementsUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
