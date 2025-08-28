package com.minecraftquietus.quietus.client.multiplayer;

import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.minecraftquietus.quietus.client.packet.SkillTreeUpdatePacket;
import com.minecraftquietus.quietus.skilltree.SkillCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSkillTree {
    private final Minecraft minecraft;

    private ImmutableMap<ResourceLocation, SkillCategory> categories;

    public ClientSkillTree(Minecraft minecraft) {
        this.minecraft = minecraft;
        
        this.categories = ImmutableMap.of();
    }

    public ImmutableMap<ResourceLocation, SkillCategory> getCategories() {
        return this.categories;
    }

    public void update(SkillTreeUpdatePacket packet) {
        ImmutableMap.Builder<ResourceLocation,SkillCategory> immutablemap$builder = ImmutableMap.builder();
        packet.skillTree().forEach((resourceLocation, skillCategory) -> immutablemap$builder.put(resourceLocation,skillCategory));
        this.categories = immutablemap$builder.build();
    }
}
