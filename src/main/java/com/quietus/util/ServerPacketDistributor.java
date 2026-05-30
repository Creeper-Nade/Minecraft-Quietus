package com.quietus.util;

import com.quietus.server.packet.GrapplingJumpReleasePacket;
import com.quietus.server.packet.SkillTreeGUIRequest;
import com.quietus.skilltree.SkillTreeNode;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import static com.quietus.Quietus.MODID;

public class ServerPacketDistributor {

    public static void requestSkillTreeUpdate() {
        ClientPacketDistributor.sendToServer(new SkillTreeGUIRequest(false, Identifier.fromNamespaceAndPath(MODID, "none")));
    }
    public static void requestSkillTreeUpgrade(SkillTreeNode node) {
        ClientPacketDistributor.sendToServer(new SkillTreeGUIRequest(true, node.getId()));
    }
    public static void sendGrappleJumpPackToServer() {
        ClientPacketDistributor.sendToServer(new GrapplingJumpReleasePacket());
    }
}
