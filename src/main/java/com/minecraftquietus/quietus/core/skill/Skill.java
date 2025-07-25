package com.minecraftquietus.quietus.core.skill;

import net.minecraft.Util;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;

public class Skill {
    protected final ResourceKey<Skill> id;
    protected final String descriptionId;
    protected static DependantName<Skill,String> descriptionIdDependant = id -> Util.makeDescriptionId("skill", id.location());
    
    private final int maxLevel;


    public Skill(ResourceKey<Skill> id, int maxLevel) {
        this.id = id;
        this.descriptionId = descriptionIdDependant.get(id);
        this.maxLevel = maxLevel;
    }

    public int maxLevel() {
        return this.maxLevel;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }
}
