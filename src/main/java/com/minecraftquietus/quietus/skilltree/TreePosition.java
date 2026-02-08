package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.minecraftquietus.quietus.client.screens.skill_tree.SkillTreeWidget;
import com.mojang.logging.LogUtils;

import net.minecraft.util.RandomSource;

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

        Map<SkillTreeNode,Integer> layer = this.basicLayer(topOrder);

        Map<SkillTreeNode,Integer> constrainedLayer = widthConstrainedLayer(layer, skillCategory.maxWidth());

        Map<Integer,Integer> layerToCount = new HashMap<>();
        constrainedLayer.forEach((n,l) -> {
            int order = layerToCount.getOrDefault(l, 0);
            Vertex v = new Vertex(order, l);
            this.vertices.put(n, v);
            layerToCount.put(l, order+1);
        });
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
        Map<SkillTreeNode,Integer> out = new LinkedHashMap<>();

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

    private Map<SkillTreeNode,Integer> widthConstrainedLayer(Map<SkillTreeNode,Integer> basicLayerMap, int width) {
        Map<SkillTreeNode,Integer> out = new LinkedHashMap<>();
        
        int l = 0;
        int o = 0;
        int count = 0;

        while (out.size() < basicLayerMap.size()) {
            for (Entry<SkillTreeNode,Integer> entry : basicLayerMap.entrySet()) {
                SkillTreeNode node = entry.getKey();
                int layer = entry.getValue();
                
                if (layer == l) {
                    count += 1;
                    if (count > width) {
                        o += 1;
                        count = 0;
                    }
                    out.put(node, o);
                }
            }
            
            l += 1;
            o += 1;
            count = 0;
        }

        return out;

    }

    public Map<SkillTreeNode,Vertex> getVertices() {
        return this.vertices;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    private static class Node {
        final List<Node> parents = new ArrayList<>();
        final List<Node> children = new ArrayList<>();

        final Optional<SkillTreeNode> node;

        Node(@Nullable SkillTreeNode node) {
            this.node = node == null ? Optional.empty() : Optional.of(node);
        }

        void addParent(Node n) {
            this.parents.add(n);
        }

        void addChild(Node n) {
            this.children.add(n);
        }
    }

    public static record Vertex(
        int x,
        int y
    ) {}

    public static record Edge(
        int startX,
        int startY,
        int midY,
        int finalX,
        int finalY,
        boolean dotted
    ) {}
}
