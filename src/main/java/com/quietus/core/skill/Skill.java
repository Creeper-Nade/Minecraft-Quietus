package com.quietus.core.skill;


import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

public class Skill {
    public enum Type {
        INT,
        FLOAT,
        DOUBLE
    }

    protected final ResourceKey<Skill> id;
    protected final String descriptionId;
    protected static DependantName<Skill,String> descriptionIdDependant = id -> Util.makeDescriptionId("skill", id.identifier());
    
    private final Type type;
    private final Number maxLevel;

    public Skill(ResourceKey<Skill> id, Type type, Number maxLevel) {
        this.id = id;
        this.descriptionId = descriptionIdDependant.get(id);
        this.type = type;
        this.maxLevel = maxLevel;
    }

    public Skill(ResourceKey<Skill> id, int maxLevel) {
        this(id, Type.INT, maxLevel);
    }

    public Type getType() {
        return this.type;
    }

    public Number maxLevel() {
        return this.maxLevel;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }
}
