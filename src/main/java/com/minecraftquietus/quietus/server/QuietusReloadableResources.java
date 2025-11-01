package com.minecraftquietus.quietus.server;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.server.resources.ServerSkillTreeManager;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPoint;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.PacketDistributor;


public class QuietusReloadableResources {
    private static boolean open = false; // marks whether or not the resources have ever been loaded or initiated

    protected static ServerSkillTreeManager skillTreeManager;

    /* this listener method is added onto event bus in Quietus.java */
    public static void onServerResourceReload(AddServerReloadListenersEvent event) {
        open(); 
        
        skillTreeManager = new ServerSkillTreeManager(event.getRegistryAccess(), SkillCategory.CODEC, SkillPoint.CODEC);
        event.addListener(ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree"), skillTreeManager::reload);
        //PacketDistributor.sendToAllPlayers(new SkillTreeUpdatePacket(skillTree.get())); // also send packet to update skill trees on clients
        //ClientSkillTreePayloadHandler.getSkillTree().update(new SkillTreeUpdatePacket(skillTreeManager.getCategories()));
    }

    /**
     * Access the server's reloadable resources for the mod Quietus
     * @return map of locations of the skill categories to themselves. May be null (i.e. if resources never reloaded).
     * @throws IllegalStateException resources already closed (i.e. server shut down)
     */
    @Nullable
    public static Map<ResourceLocation,SkillCategory> getSkillCategories() {
        if (!open) throw new IllegalStateException("Attemping to access closed resources");
        return Objects.isNull(skillTreeManager) ? null : skillTreeManager.getCategories();
    }


    public static boolean isOpen() {
        return open;
    }

    public static void open() {
        open = true;
    }
    public static void close() {
        open = false;
    }
}
