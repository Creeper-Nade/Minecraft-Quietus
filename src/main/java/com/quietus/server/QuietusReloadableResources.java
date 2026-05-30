package com.quietus.server;

import static com.quietus.Quietus.MODID;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.quietus.server.handler.SkillTreeGUIPayloadHandler;
import com.quietus.server.resources.ServerSkillTreeManager;
import com.quietus.skilltree.SkillCategory;
import com.quietus.skilltree.SkillPoint;

import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;


public class QuietusReloadableResources {
    private static boolean open = false; // marks whether or not the resources have ever been loaded or initiated

    protected static ServerSkillTreeManager skillTreeManager;

    /* this listener method is added onto event bus in Quietus.java */
    public static void onAddingServerResourceReloadListeners(AddServerReloadListenersEvent event) {
        open(); 
        
        skillTreeManager = new ServerSkillTreeManager(event.getRegistryAccess(), SkillCategory.CODEC, SkillPoint.CODEC);
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "skill_tree"), skillTreeManager::reload);

        SkillTreeGUIPayloadHandler.setManager(skillTreeManager);
    }

    /**
     * Access the server's reloadable resources for the mod Quietus
     * @return map of locations of the skill categories to themselves. May be null (i.e. if resources never reloaded).
     * @throws IllegalStateException resources already closed (i.e. server shut down)
     */
    @Nullable
    public static Map<Identifier,SkillCategory> getSkillCategories() throws IllegalStateException {
        if (!open) throw new IllegalStateException("Attemping to access closed resources");
        return Objects.isNull(skillTreeManager) ? null : skillTreeManager.getCategories();
    }

    /**
     * Access the server's reloadable resources for the mod Quietus
     * @return set of advancements resource locations, may be null (i.e. if resources never reloaded).
     *      Note: the specified resource locations may not point to actual resource locations of loaded advancements,
     *      since this is read from the loaded skill point data, without checking whether or not the specified 
     *      advancements to that path are already loaded
     * @throws IllegalStateException resources already closed (i.e. server shut down)
     */
    @Nullable
    public static Set<Identifier> getRequiredAdvancements() throws IllegalStateException {
        if (!open) throw new IllegalStateException("Attemping to access closed resources");
        return Objects.isNull(skillTreeManager) ? null : skillTreeManager.getRequiredAdvancements();
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
