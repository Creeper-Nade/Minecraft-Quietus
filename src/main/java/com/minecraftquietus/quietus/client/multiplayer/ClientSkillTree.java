package com.minecraftquietus.quietus.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.minecraftquietus.quietus.skilltree.SkillCategory;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSkillTree {
    private ImmutableMap<ResourceLocation, SkillCategory> categories = ImmutableMap.of();

    public ImmutableMap<ResourceLocation, SkillCategory> getCategories() {
        return this.categories;
    }
}
