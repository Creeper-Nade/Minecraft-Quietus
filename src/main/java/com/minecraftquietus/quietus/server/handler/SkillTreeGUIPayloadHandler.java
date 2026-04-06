package com.minecraftquietus.quietus.server.handler;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.server.PlayerSkillTree;
import com.minecraftquietus.quietus.server.packet.SkillTreeGUIRequest;
import com.minecraftquietus.quietus.server.resources.ServerSkillTreeManager;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.minecraftquietus.quietus.util.PlayerClientPacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

/**
 * Handles packets sent from clients related to skill trees
 * sent to server-side
 */
public class SkillTreeGUIPayloadHandler {

    private static ServerSkillTreeManager manager;

    public static void setManager(ServerSkillTreeManager mg) {
        manager = mg;
    }

    public static void handleSkillTreeRequest(SkillTreeGUIRequest packet, final IPayloadContext context) {
        /* if (context.player() instanceof ServerPlayer serverPlayer) {
            SkillTreeUpdatePacket returnPacket = PlayerClientPacketDistributor.makeClientboundSkillTreePack(serverPlayer);
            context.reply(returnPacket);
        } */
        MinecraftServer server = context.player().getServer();
        if (Objects.nonNull(server)) {
            ServerPlayer player = server.getPlayerList().getPlayer(context.player().getUUID());
            if (Objects.nonNull(player)) {
                SkillTreeUpdatePacket returnPacket = PlayerClientPacketDistributor.makeClientboundSkillTreePack(player);
                context.enqueueWork(() -> {
                    if (packet.upgrade()) {
                        PlayerSkillTree skillTree = Quietus.playerData.getSkillTree(player.getUUID());
                        if (Objects.nonNull(skillTree)) {
                            if (packet.skillTreeNode() != null) {
                                SkillTreeNode node = manager.getNode(packet.skillTreeNode());
                                skillTree.addOrStartProgress(node);
                            }
                            context.reply(PlayerClientPacketDistributor.makeClientboundSkillTreePack(player));
                        }
                    } else {
                        context.reply(returnPacket);
                    }
                }).exceptionally(e -> {
                    context.disconnect(Component.translatable("quietus.client.networking.failed.skillTree", e.getMessage()));
                    return null;
                });

            }
        }


    }

}
