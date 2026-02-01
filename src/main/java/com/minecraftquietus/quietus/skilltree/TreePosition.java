package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;

public class TreePosition {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int nodePaddingWidth;
    private final int nodePaddingHeight;
    private final int nodeMarginWidth;
    private final int nodeMarginHeight;

    private final RandomSource random;

    private final Map<SkillTreeNode,Vertex> vertices = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public TreePosition(int nodePaddingWidth, int nodePaddingHeight, int nodeMarginWidth, int nodeMarginHeight, long seed) {
        this.nodePaddingWidth = nodePaddingWidth;
        this.nodePaddingHeight = nodePaddingHeight;
        this.nodeMarginWidth = nodeMarginWidth;
        this.nodeMarginHeight = nodeMarginHeight;

        this.random = RandomSource.create(seed);
    }

    public void makeGraphOf(SkillCategory skillCategory) {
        Collection<SkillTreeNode> nodes = skillCategory.getNodesMap().values();
        Map<SkillTreeNode,Integer> inDeg = nodes.stream().collect(Collectors.toMap((node) -> node, (node) -> node.parents().size()));
        
        List<SkillTreeNode> topOrder = this.topoSort(inDeg);

        topOrder.forEach((node)->LOGGER.info(node.getId().toString())); 

        Map<SkillTreeNode,Integer> layer = this.basicLayer(topOrder);

        LOGGER.info("Final: ");
        layer.forEach((node,l) -> LOGGER.info("{}: {}",node.getId(),l));
    }

    /**
         * Topological randomly sorted. 
         * As a result, different RandomSource seeds will produce some nodes put in lower layers during 
         * {@link TreePosition#widthConstrainedLayer()}, if smoe layers exceed maxWidth
         */
    private List<SkillTreeNode> topoSort(Map<SkillTreeNode,Integer> in) {
        Map<SkillTreeNode,Integer> inDeg = new HashMap<>(in);
        Queue<SkillTreeNode> queue = new ArrayDeque<>(inDeg.size());
        List<SkillTreeNode> out = new ArrayList<>(inDeg.size());
        do {
            SkillTreeNode working = queue.poll();
            if (working != null) {
                out.add(working);
                //inDeg.remove(working);
            }
            for (Entry<SkillTreeNode,Integer> i : inDeg.entrySet()) {
                SkillTreeNode key = i.getKey();
                int currentInDeg = i.getValue();
                if (key.parents().contains(working)) {
                    inDeg.put(key, currentInDeg-1);
                }
            }
            List<SkillTreeNode> addingToQueueList = inDeg.entrySet().stream()
                .filter((entry) -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            addingToQueueList.forEach(inDeg::remove);
            while (!addingToQueueList.isEmpty()) { // add to queue in random order
                int chosen = addingToQueueList.size() == 1 ? 0 : this.random.nextInt(addingToQueueList.size()-1);
                queue.add(addingToQueueList.get(chosen));
                addingToQueueList.remove(chosen);
            }
        }
        while (!queue.isEmpty());

        return out;
    }

    
    private Map<SkillTreeNode,Integer> basicLayer(List<SkillTreeNode> topOrder) {
        List<SkillTreeNode> order = new ArrayList<>(topOrder);
        Map<SkillTreeNode,Integer> out = new HashMap<>();

        /** 
         * Forward pass
         * Layer sorting using earliest layer from parents */
        for (SkillTreeNode node : order) {
            int earliest = 0;
            for (SkillTreeNode pre : node.parents()) {
                earliest = Math.max(earliest, out.getOrDefault(pre, 0)+1);
            }
            out.put(node, earliest);
        }

        LOGGER.info("before backward pass");
        out.forEach((node,l) -> LOGGER.info("{}: {}",node.getId(),l));

        /**
         * Backward pass
         * Try to degrade high vertices down to avoid long edge spans, without superceding layers of their children
         * SkillPoint: skips ones with layout top=true, so that they stay as high from the forward pass
         */
        for (SkillTreeNode node : order.reversed()) {
            if (node.children.isEmpty()) { // skip leaves
                continue;
            }
            int latest = Integer.MAX_VALUE;
            for (SkillTreeNode child : node.children) {
                latest = Math.min(latest, out.getOrDefault(child, 1)-1);
            }
            out.put(node, latest);
        }
        
        return out;
    }

    public static record Vertex(
        int x,
        int y
    ) {}

    public static record Edge(
        int startX,
        int startY,
        int finalX,
        int finalY,
        boolean dotted
    ) {}
}
