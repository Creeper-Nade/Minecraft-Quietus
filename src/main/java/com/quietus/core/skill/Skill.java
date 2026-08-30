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
    protected final String idDisplay;
    private final String displayTemplate;
    protected static DependantName<Skill,String> descriptionIdDependant = id -> Util.makeDescriptionId("skill", id.identifier());
    
    private final Type type;
    private final Number maxLevel;

    public Skill(ResourceKey<Skill> id, Type type, Number maxLevel, String displayTemplate) {
        this.id = id;
        this.idDisplay = descriptionIdDependant.get(id);
        this.type = type;
        this.maxLevel = maxLevel;
        this.displayTemplate = displayTemplate;
    }

    public Skill(ResourceKey<Skill> id, Type type, Number maxLevel) {
        this(id, type, maxLevel, "skill.quietus.default.template");
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

    public String getIdDisplay() {
        return this.idDisplay;
    }

    public String getDisplayTemplate() {
        return this.displayTemplate;
    }
}
