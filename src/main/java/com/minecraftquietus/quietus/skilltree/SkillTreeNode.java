package com.minecraftquietus.quietus.skilltree;

import java.util.Collection;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeNode {
    
    private final ResourceLocation id;
    private final SkillPoint skillPoint;

    private final Set<SkillTreeNode> parents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeNode> mustParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeNode> orParents = new ReferenceOpenHashSet<>();
    protected final Set<SkillTreeNode> children = new ReferenceOpenHashSet<>();

    private int treeX;
    private int treeY;

    public SkillTreeNode(ResourceLocation id, SkillPoint skillPoint) {
        this.id = id;
        this.skillPoint = skillPoint;
    }

    public void setParents(Collection<SkillTreeNode> col) {
        this.parents.clear();
        this.parents.addAll(col);

        this.mustParents.clear();
        this.orParents.clear();
        col.forEach((node) -> {
            if (this.getSkillPoint().unlock().prerequisites().getAllMustParents().contains(node.getId()))
                this.mustParents.add(node);
            if (this.getSkillPoint().unlock().prerequisites().getAllOrParents().contains(node.getId()))
                this.orParents.add(node);
        });
    }

    public void addChild(SkillTreeNode child) {
        this.children.add(child);
    }

    public void setTreeLocation(int x, int y) {
        this.treeX = x;
        this.treeY = y;
    }
    public void addConnectivityPoint(int x, int y) {
        
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
    
    public Collection<SkillTreeNode> parents() {
        return this.parents;
    }
    public Collection<SkillTreeNode> mustParents() {
        return this.mustParents;
    }
    public Collection<SkillTreeNode> orParents() {
        return this.orParents;
    }

    public int getTreeX() {
        return this.treeX;
    }
    public int getTreeY() {
        return this.treeY;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof SkillTreeNode otherNode) {
            if (this.id.equals(otherNode.id)) {
                return true;
            }
        }
        return false;
    }
}
