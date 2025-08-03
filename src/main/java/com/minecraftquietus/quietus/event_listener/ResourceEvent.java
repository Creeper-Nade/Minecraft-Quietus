package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.server.ServerSkillTreeManager;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPoint;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;


public class ResourceEvent {
    
    public static void onServerResourceReload(AddServerReloadListenersEvent event) {
        ServerSkillTreeManager skillTreeManager = new ServerSkillTreeManager(event.getRegistryAccess(), SkillCategory.CODEC, SkillPoint.CODEC);
        event.addListener(ResourceLocation.parse("quietus:skill_tree"), skillTreeManager::reload);
    }
}
