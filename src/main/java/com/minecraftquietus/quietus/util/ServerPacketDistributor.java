package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.server.packet.SkillTreeGUIRequest;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ServerPacketDistributor {

    public static void requestSkillTreeUpdate() {
        PacketDistributor.sendToServer(new SkillTreeGUIRequest(false, Identifier.fromNamespaceAndPath(MODID, "none")));
    }
    public static void requestSkillTreeUpgrade(SkillTreeNode node) {
        PacketDistributor.sendToServer(new SkillTreeGUIRequest(true, node.getId()));
    }
}
