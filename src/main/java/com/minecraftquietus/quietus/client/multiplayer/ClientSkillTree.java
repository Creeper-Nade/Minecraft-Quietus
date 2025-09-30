package com.minecraftquietus.quietus.client.multiplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillCategoryProgress;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSkillTree {
    private final Minecraft minecraft;

    private ImmutableMap<ResourceLocation, SkillCategory> categories;

    private Map<SkillCategory, SkillCategoryProgress> progress;

    public ClientSkillTree(Minecraft minecraft) {
        this.minecraft = minecraft;
        
        this.categories = ImmutableMap.of();
        this.progress = new HashMap<>();
    }

    public ImmutableMap<ResourceLocation, SkillCategory> getCategories() {
        return this.categories;
    }
    public Map<SkillCategory, SkillCategoryProgress> getProgress() {
        return this.progress;
    }

    public void update(SkillTreeUpdatePacket packet) {
        ImmutableMap.Builder<ResourceLocation,SkillCategory> immutablemap$builder = ImmutableMap.builder();
        packet.skillTree().forEach((resourceLocation, skillCategory) -> immutablemap$builder.put(resourceLocation,skillCategory));
        this.categories = immutablemap$builder.build();
    }
}
