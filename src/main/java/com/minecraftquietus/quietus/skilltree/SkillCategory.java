package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.resources.ResourceLocation;


public class SkillCategory {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, SkillTreeNode> nodes = new Object2ObjectOpenHashMap<>();
    private final Set<SkillTreeNode> roots = new ObjectLinkedOpenHashSet<>();
    private final Set<SkillTreeNode> dependants = new ObjectLinkedOpenHashSet<>();
    private final Prerequisites prerequisites;
    private final ResourceLocation id;

    /* For CODEC decoding only, hence this constructor will not be visible */
    private SkillCategory(Prerequisites prerequisites) {
        this.prerequisites = prerequisites;
        this.id = null;
    }
    
    /* Constructs actual usable SkillCategory instances. */
    public SkillCategory(ResourceLocation id, Prerequisites prerequisites) {
        this.id = id;
        this.prerequisites = prerequisites;
    }

    public static final Codec<SkillCategory> CODEC = RecordCodecBuilder.create(
        (instance) -> instance.group(
            Prerequisites.CODEC.optionalFieldOf("prerequisites",Prerequisites.EMPTY).forGetter(SkillCategory::getPrerequisites)
        ).apply(instance, SkillCategory::new) // should use the private constructor without id.
    );

    public void addAll(Map<ResourceLocation, SkillPoint> map) {
        List<ResourceLocation> list = new ArrayList<>(map.keySet());

        while (!list.isEmpty()) {
            if (!list.removeIf((location) -> this.tryInsert(location, map.get(location)))) {
                LOGGER.error("Couldn't load skill tree nodes: {}", list);
                break;
            }
        }

        LOGGER.info("Loaded {} skill tree nodes for category {}", this.nodes.size(), this.id.toString());
    }

    private boolean tryInsert(ResourceLocation location, SkillPoint skillPoint) {
        Set<ResourceLocation> parents = skillPoint.prerequisites().getAllParents();
        List<SkillTreeNode> parentNodes = parents.isEmpty() ? new ArrayList<>() : parents.stream().map(this.nodes::get).collect(Collectors.toList());

        if (parentNodes.contains(null) && !parents.isEmpty()) { // this node should have parents, and that any of its parents are not created yet.
            return false;
        } else {
            SkillTreeNode node = new SkillTreeNode(location, skillPoint);

            this.nodes.put(location, node);
            
            if (!parentNodes.contains(null)) { // all parents already created
                /* Updating parents of this node and children of parents  */
                node.setParents(parentNodes);
                parentNodes.forEach((parent) -> parent.addChild(node));

                /* Updating roots and dependants */
                if (parents.isEmpty()) { // 没腐没木
                    this.roots.add(node);
                } else {
                    this.dependants.add(node);
                }

                return true;
            } else { // not all parents already created, AND this node should have more parents created: leave this node to be further improved
                return false;
            }
            
        }
    }

    public Prerequisites getPrerequisites() {
        return this.prerequisites;
    }

}
