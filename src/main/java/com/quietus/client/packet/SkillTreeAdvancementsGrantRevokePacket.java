package com.quietus.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

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
