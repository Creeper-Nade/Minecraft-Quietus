package com.minecraftquietus.quietus.event_listener;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.server.ServerSkillTreeManager;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPoint;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.PacketDistributor;


public class QuietusReloadableResources {
    private static boolean open = false; // marks whether or not the resources have ever been loaded (so as initiated)

    private static ServerSkillTreeManager skillTree;

    /* this listener method is added onto event bus in Quietus.java */
    public static void onServerResourceReload(AddServerReloadListenersEvent event) {
        open(); 
        
        skillTree = new ServerSkillTreeManager(event.getRegistryAccess(), SkillCategory.CODEC, SkillPoint.CODEC);
        event.addListener(ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree"), skillTree::reload);
        //PacketDistributor.sendToAllPlayers(new SkillTreeUpdatePacket(skillTree.get())); // also send packet to update skill trees on clients
        ClientSkillTreePayloadHandler.getSkillTree().update(new SkillTreeUpdatePacket(skillTree.get()));
    }

    @Nullable
    public static Map<ResourceLocation,SkillCategory> getCategories() {
        return (Objects.isNull(skillTree) || !open) ? null : skillTree.get();
    }


    public static void open() {
        open = true;
    }
    public static void close() {
        open = false;
    }
}
