package com.minecraftquietus.quietus.client.multiplayer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillPointProgress;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSkillTree {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;

    private ImmutableMap<ResourceLocation, SkillCategory> categories = ImmutableMap.of();

    private final Map<ResourceLocation,SkillPointProgress.ClientData> progresses = new LinkedHashMap<>();

    private final Map<ResourceLocation,ClientSkillTreeListener> listeners = new HashMap<>();

    public ClientSkillTree(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addListener(SkillTreeNode node, ClientSkillTreeListener listener) {
        LOGGER.info("added listener: {}", node.getId());
        this.listeners.put(node.getId(), listener);
        SkillPointProgress.ClientData progress = this.getOrStartProgress(node);
        listener.onClientSkillTreeUpdate(progress.times(), progress.maxAmount(), progress.progressAmount());
    }
    public void removeListener(SkillTreeNode node) {
        LOGGER.info("removed listener: {}", node.getId());
        this.listeners.remove(node.getId());
    }

    private SkillPointProgress.ClientData startProgress(SkillTreeNode node) {
        if (!this.progresses.containsKey(node.getId())) {
            SkillPointProgress.ClientData progress = new SkillPointProgress.ClientData(0, node.getSkillPoint().maxAmount(), node.getSkillPoint().progressAmount());
            this.progresses.put(node.getId(), progress);
            return progress;
        }
        return this.progresses.get(node.getId());
    }

    public ImmutableMap<ResourceLocation, SkillCategory> getCategories() {
        return this.categories;
    }
    public Map<ResourceLocation,SkillPointProgress.ClientData> getProgressesMap() {
        return this.progresses;
    }
    public SkillPointProgress.ClientData getOrStartProgress(SkillTreeNode node) {
        if (this.progresses.containsKey(node.getId())) {
            return this.getProgress(node);
        } else {
            return this.startProgress(node);
        }

    }
    private SkillPointProgress.ClientData getProgress(SkillTreeNode node) {
        return this.progresses.get(node.getId());
    }

    public void update(SkillTreeUpdatePacket packet) {
        LOGGER.info("update!");
        ImmutableMap.Builder<ResourceLocation,SkillCategory> immutablemap$builder = ImmutableMap.builder();
        immutablemap$builder.putAll(packet.skillTree());
        this.categories = immutablemap$builder.build();
        //this.progresses.clear();
        packet.progresses().forEach(
            (resourceLocation, progress) -> {
                SkillTreeNode node = null;
                for (SkillCategory category : packet.skillTree().values()) {
                    node = category.getNode(resourceLocation);
                }
                if (node == null) {
                    LOGGER.info("Ignoring skill tree progress {} received from server - this skill tree node does not exist?", resourceLocation.toString());
                } else {
                    LOGGER.info("Node: {}", node.getId());
                    this.progresses.put(node.getId(), progress);
                    if (this.listeners.containsKey(node.getId()))
                    this.listeners.get(node.getId()).onClientSkillTreeUpdate(progress.times(), progress.maxAmount(), progress.progressAmount());
                }
            }
        );
    }
}
