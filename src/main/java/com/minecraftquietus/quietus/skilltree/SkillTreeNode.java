package com.minecraftquietus.quietus.skilltree;

import java.util.Collection;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeNode {
    
    private final ResourceLocation id;
    private final SkillPoint skillPoint;

    protected final Set<SkillTreeNode> parents = new ReferenceOpenHashSet<>();
    protected final Set<SkillTreeNode> children = new ReferenceOpenHashSet<>();

    public SkillTreeNode(ResourceLocation id, SkillPoint skillPoint) {
        this.id = id;
        this.skillPoint = skillPoint;
        // all prerequisite nodes are parents
    }

    public void setParents(Collection<SkillTreeNode> col) {
        this.parents.clear();
        this.parents.addAll(col);
    }

    public void addChild(SkillTreeNode child) {
        this.children.add(child);
    }

    public SkillPoint getSkillPoint() {
        return this.skillPoint;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean isRoot() {
        return this.parents.size() == 0;
    }
}
