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

    private final Map<SkillTreeNode,SkillPointProgress.ClientData> progresses = new LinkedHashMap<>();

    public ClientSkillTree(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private SkillPointProgress.ClientData startProgress(SkillTreeNode node) {
        if (!this.progresses.containsKey(node)) {
            SkillPointProgress.ClientData progress = new SkillPointProgress.ClientData(0, node.getSkillPoint().maxAmount(), node.getSkillPoint().progressAmount());
            this.progresses.put(node, progress);
            return progress;
        }
        return this.progresses.get(node);
    }

    public ImmutableMap<ResourceLocation, SkillCategory> getCategories() {
        return this.categories;
    }
    public Map<SkillTreeNode,SkillPointProgress.ClientData> getProgressesMap() {
        return this.progresses;
    }
    public SkillPointProgress.ClientData getOrStartProgress(SkillTreeNode node) {
        if (this.progresses.containsKey(node)) {
            return this.getProgress(node);
        } else {
            return this.startProgress(node);
        }

    }
    private SkillPointProgress.ClientData getProgress(SkillTreeNode node) {
        return this.progresses.get(node);
    }

    public void update(SkillTreeUpdatePacket packet) {
        LOGGER.info("update!");
        ImmutableMap.Builder<ResourceLocation,SkillCategory> immutablemap$builder = ImmutableMap.builder();
        packet.skillTree().forEach((resourceLocation, skillCategory) -> immutablemap$builder.put(resourceLocation,skillCategory));
        this.categories = immutablemap$builder.build();
        packet.progresses().forEach(
            (resourceLocation, progress) -> {
                SkillTreeNode node = null;
                for (SkillCategory category : packet.skillTree().values()) {
                    node = category.getNode(resourceLocation);
                }
                if (node == null) {
                    LOGGER.info("Ignoring skill tree progress {} received from server - this skill tree node does not exist?", resourceLocation.toString());
                } else {
                    this.progresses.put(node, progress);
                }
            }
        );
    }
}
