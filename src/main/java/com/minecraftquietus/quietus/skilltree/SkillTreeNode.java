package com.minecraftquietus.quietus.skilltree;

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
        /* this.skillPoint.prerequisites().parentNodes().forEach(set -> {
            set.forEach(location -> {
                this.parents.add(QuietusRegistries.SKILL_NODE_REGISTRY.getValue(location));
            });
        }); */
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
