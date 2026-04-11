package com.minecraftquietus.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPointProgress;

public record SkillTreeAdvancementsGrantRevokePacket(
    Identifier advancementId,
    boolean isGrant
) implements CustomPacketPayload {
    
    public static final Type<SkillTreeAdvancementsGrantRevokePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID,"skill_tree_advancements_grant_revoke"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeAdvancementsGrantRevokePacket> STREAM_CODEC = StreamCodec.composite(    
        Identifier.STREAM_CODEC, SkillTreeAdvancementsGrantRevokePacket::advancementId,
        ByteBufCodecs.BOOL, SkillTreeAdvancementsGrantRevokePacket::isGrant,
        SkillTreeAdvancementsGrantRevokePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
