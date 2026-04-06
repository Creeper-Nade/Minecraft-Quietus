package com.minecraftquietus.quietus.server.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.UUID;

import com.minecraftquietus.quietus.server.QuietusReloadableResources;

import static com.minecraftquietus.quietus.Quietus.MODID;


/**
 * Packet client to server request for sending skill tree data to client
 * @param upgrade true if should upgrade skillTreeNode.
 * @param skillTreeNode if upgrade is true, server-side will add progress to this if exists
 */
public record SkillTreeGUIRequest(
    boolean upgrade,
    ResourceLocation skillTreeNode
) implements CustomPacketPayload {

    public static final Type<SkillTreeGUIRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeGUIRequest> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, SkillTreeGUIRequest::upgrade,
        ResourceLocation.STREAM_CODEC, SkillTreeGUIRequest::skillTreeNode,
        SkillTreeGUIRequest::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
