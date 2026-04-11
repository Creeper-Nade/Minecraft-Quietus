package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.server.packet.GrapplingJumpReleasePacket;
import com.minecraftquietus.quietus.server.packet.SkillTreeGUIRequest;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import static com.minecraftquietus.quietus.Quietus.MODID;

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
