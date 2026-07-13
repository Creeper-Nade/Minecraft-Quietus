package com.quietus.skilltree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;

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
        if (skillCategory.getNodesMap().isEmpty()) { // skip and do nothing for empty category
            return;
        }
        Collection<SkillTreeNode> nodes = skillCategory.getNodesMap().values();
        Collection<SkillTreeNode> roots = skillCategory.getRoots();
        Map<SkillTreeNode,Integer> inDeg = nodes.stream().collect(Collectors.toMap((node) -> node, (node) -> node.parents().size()));
        
        List<SkillTreeNode> topOrder = this.topoSort(inDeg);

        Map<SkillTreeNode,Integer> layer = this.basicLayer(topOrder);

        LinkedHashMap<SkillTreeNode,Integer> constrainedLayer = widthConstrainedLayer(layer, skillCategory.maxWidth());

        Map<SkillTreeNode,Node> conversionMap = conversionMap(nodes);

        Map<Node,Integer> layersWithDummies = 
            addDummyVertices(
                constrainedLayer.entrySet().stream().collect(Collectors.toMap(entry -> conversionMap.get(entry.getKey()), Map.Entry::getValue, (existing, replacement) -> existing, LinkedHashMap::new)), 
                roots.stream().map(conversionMap::get).collect(Collectors.toList())
            );

        List<List<Node>> layeredNodes = groupNodesByLayer(layersWithDummies);
        layeredNodes.forEach(this::updateIndices);
        minimizeCrossings(layeredNodes);
        makeCoordinates(layeredNodes);
        makeEdges(layeredNodes);
    }

    /**
         * Topological randomly sorted. 
         * As a result, different RandomSource seeds will produce some nodes put in lower layers during 
         * {@link TreePosition#widthConstrainedLayer}, if some layers exceed maxWidth
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
            if (node.children.isEmpty() || node.getSkillPoint().layout().top()) { // skip leaves or top=true
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

    private LinkedHashMap<SkillTreeNode,Integer> widthConstrainedLayer(Map<SkillTreeNode,Integer> basicLayerMap, int width) {
        LinkedHashMap<SkillTreeNode,Integer> out = new LinkedHashMap<>();
        
        int l = Collections.min(basicLayerMap.values());
        int o = l;
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

    /**
     * Make a mapping of given SkillTreeNode to new initiated Node
     * @param col {@link java.util.Collection} of SkillTreeNode
     * @return conversion map
     */
    private Map<SkillTreeNode,Node> conversionMap(Collection<SkillTreeNode> col) {
        Map<SkillTreeNode,Node> conversionMap = col.stream().collect(Collectors.toMap(e->e, e -> new Node(e), (existing, replacement) -> existing, HashMap::new));
        col.forEach((skillTreeNode) -> conversionMap.get(skillTreeNode).addParents(skillTreeNode.parents().stream().map(conversionMap::get).collect(Collectors.toList())));
        col.forEach((skillTreeNode) -> conversionMap.get(skillTreeNode).addChildren(skillTreeNode.children.stream().map(conversionMap::get).collect(Collectors.toList())));
        col.forEach((skillTreeNode) -> conversionMap.get(skillTreeNode).noDummyParents.addAll(skillTreeNode.parents()));
        col.forEach((skillTreeNode) -> conversionMap.get(skillTreeNode).noDummyChildren.addAll(skillTreeNode.children));
        return conversionMap;
    }

    private Map<Node,Integer> addDummyVertices(LinkedHashMap<Node,Integer> nodeLayersMap, Collection<Node> roots) {
        for (Node node : roots) {
            node.recursiveMakeDummy(nodeLayersMap);
        }

        return nodeLayersMap;
    }

    private List<List<Node>> groupNodesByLayer(Map<Node, Integer> layerMap) {
        int maxLayer = layerMap.values().stream().max(Integer::compare).orElse(0);
        List<List<Node>> layers = new ArrayList<>(maxLayer + 1);
        for (int i = 0; i <= maxLayer; i++) {
            layers.add(new ArrayList<>());
        }
        for (Map.Entry<Node, Integer> entry : layerMap.entrySet()) {
            layers.get(entry.getValue()).add(entry.getKey());
        }
        return layers;
    }

    private void minimizeCrossings(List<List<Node>> layers) {
        // Barycenter heuristic
        for (int i = 0; i < 10; i++) {
            // Down sweep
            for (int l = 1; l < layers.size(); l++) {
                barycenterSort(layers.get(l), layers.get(l - 1), true);
            }
            // Up sweep
            for (int l = layers.size() - 2; l >= 0; l--) {
                barycenterSort(layers.get(l), layers.get(l + 1), false);
            }
        }

        // Sifting
        for (int l = 0; l < layers.size(); l++) {
            siftLayer(layers, l);
        }
    }

    private void barycenterSort(List<Node> layer, List<Node> relativeLayer, boolean parents) {
        for (Node node : layer) {
            double sum = 0;
            int count = 0;
            Collection<Node> relatives = parents ? node.parents : node.children;
            for (Node relative : relatives) {
                if (relativeLayer.contains(relative)) {
                    sum += relative.tempIndex;
                    count++;
                }
            }
            node.barycenter = count == 0 ? node.tempIndex : sum / count;
        }
        layer.sort(Comparator.comparingDouble(n -> n.barycenter));
        updateIndices(layer);
    }

    private void updateIndices(List<Node> layer) {
        for (int i = 0; i < layer.size(); i++) {
            layer.get(i).tempIndex = i;
        }
    }

    private void siftLayer(List<List<Node>> layers, int layerIndex) {
        List<Node> layer = layers.get(layerIndex);
        List<Node> nodesToSift = new ArrayList<>(layer);
        shuffle(nodesToSift);

        for (Node node : nodesToSift) {
            int bestPos = node.tempIndex;
            int minCrossings = Integer.MAX_VALUE;

            int currentPos = layer.indexOf(node);
            layer.remove(currentPos);

            for (int i = 0; i <= layer.size(); i++) {
                layer.add(i, node);
                updateIndices(layer);
                int crossings = countLocalCrossings(layers, layerIndex);
                if (crossings < minCrossings) {
                    minCrossings = crossings;
                    bestPos = i;
                }
                layer.remove(i);
            }
            layer.add(bestPos, node);
            updateIndices(layer);
        }
    }

    private int countLocalCrossings(List<List<Node>> layers, int layerIndex) {
        int crossings = 0;
        if (layerIndex > 0) {
            crossings += countCrossingsBetween(layers.get(layerIndex - 1), layers.get(layerIndex));
        }
        if (layerIndex < layers.size() - 1) {
            crossings += countCrossingsBetween(layers.get(layerIndex), layers.get(layerIndex + 1));
        }
        return crossings;
    }

    private int countCrossingsBetween(List<Node> upper, List<Node> lower) {
        int crossings = 0;
        for (int i = 0; i < upper.size(); i++) {
            Node u1 = upper.get(i);
            for (int j = i + 1; j < upper.size(); j++) {
                Node u2 = upper.get(j);
                for (Node v1 : u1.children) {
                    if (!lower.contains(v1)) continue;
                    for (Node v2 : u2.children) {
                        if (!lower.contains(v2)) continue;
                        if (v1.tempIndex > v2.tempIndex) crossings++;
                    }
                }
            }
        }
        return crossings;
    }

    private void shuffle(List<Node> list) {
        for (int i = list.size(); i > 1; i--) {
            int j = this.random.nextInt(i);
            Collections.swap(list, i - 1, j);
        }
    }

    private void makeCoordinates(List<List<Node>> layers) {
        int maxNodesInLayer = layers.stream().mapToInt(List::size).max().orElse(0);
        int maxWidth = maxNodesInLayer * (this.nodePaddingWidth + this.nodeMarginWidth) - this.nodeMarginWidth;

        for (int i = 0; i < layers.size(); i++) {
            List<Node> layer = layers.get(i);
            int layerWidth = layer.size() * (this.nodePaddingWidth + this.nodeMarginWidth) - this.nodeMarginWidth;
            int startX = (maxWidth - layerWidth) / 2;

            for (int j = 0; j < layer.size(); j++) {
                Node node = layer.get(j);
                int x = startX + j * (this.nodePaddingWidth + this.nodeMarginWidth);
                int y = i * (this.nodePaddingHeight + this.nodeMarginHeight);
                node.x = x;
                node.y = y;
                if (node.node.isPresent()) {
                    this.vertices.put(node.node.get(), new Vertex(x, y));
                }
            }
        }
    }

    private void makeEdges(List<List<Node>> layers) {
        for (List<Node> layer : layers) {
            for (Node node : layer) {
                for (Node child : node.children) {
                    Set<SkillTreeNode> ultimateSources = node.node.map(Set::of).orElse(node.noDummyParents);
                    Set<SkillTreeNode> ultimateDestinations = child.node.map(Set::of).orElse(child.noDummyChildren);

                    boolean isDotted = false;
                    // An edge is dotted if any of the logical paths it represents is an "or" dependency.
                    for (SkillTreeNode source : ultimateSources) {
                        for (SkillTreeNode destination : ultimateDestinations) {
                            if (destination.orParents().contains(source)) {
                                isDotted = true;
                                break;
                            }
                        }
                        if (isDotted) break;
                    }

                    int startX = node.x + this.nodePaddingWidth / 2;
                    int startY = node.y + this.nodePaddingHeight / 2 - 1;
                    int finalX = child.x + this.nodePaddingWidth / 2;
                    int finalY = child.y + this.nodePaddingHeight / 2;
                    int midY = startY + (finalY - startY) / 2;
                    this.edges.add(new Edge(startX, startY, midY, finalX, finalY, isDotted));
                }
            }
        }
    }

    public Map<SkillTreeNode,Vertex> getVertices() {
        return this.vertices;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    private static class Node {
        final Set<Node> parents = new HashSet<>();
        final Set<Node> children = new HashSet<>();

        final Set<SkillTreeNode> noDummyParents = new HashSet<>();
        final Set<SkillTreeNode> noDummyChildren = new HashSet<>();

        final Optional<SkillTreeNode> node; // if Optional.empty(), this is a dummy node

        int x;
        int y;
        int tempIndex;
        double barycenter;

        Node(@Nullable SkillTreeNode node) {
            this.node = node == null ? Optional.empty() : Optional.of(node);
        }

        void addParents(Collection<Node> n) {
            this.parents.addAll(n);
        }

        void addChildren(Collection<Node> n) {
            this.children.addAll(n);
        }

        /**
         * Used Gemini because I was tired
         * @param layerMap
         */
        void recursiveMakeDummy(Map<Node,Integer> layerMap) {
            if (this.children.isEmpty()) { // is a leaf
                return;
            }

            int currentLayer = layerMap.get(this);

            // 1. Identify "Long" children (destination layer > current layer + 1)
            Set<Node> longSpanningChildren = this.children.stream()
                    .filter(child -> layerMap.get(child) - currentLayer > 1)
                    .collect(Collectors.toSet());

            // If no long edges, just recurse on normal children and exit
            if (longSpanningChildren.isEmpty()) {
                // Create a copy to avoid concurrent modification issues if recursion modifies the list
                for (Node child : new ArrayList<>(this.children)) {
                    child.recursiveMakeDummy(layerMap);
                }
                return;
            }

            // 2. Determine the "Real" targets for these long edges
            // This acts as the "Signature" for finding a reusable dummy.
            Set<SkillTreeNode> targetRealNodes = longSpanningChildren.stream()
                    .flatMap(node -> node.node.map(Stream::of).orElseGet(() -> node.noDummyChildren.stream()))
                    .collect(Collectors.toSet());

            // 3. Check for an EXISTING matching dummy at (Layer + 1)
            // We look for a Dummy Node at the next layer that targets exactly the same real nodes.
            Optional<Node> existingDummy = layerMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == currentLayer + 1) // Must be at Layer + 1
                    .map(Map.Entry::getKey)
                    .filter(node -> node.node.isEmpty()) // Must be a dummy
                    .filter(node -> node.noDummyChildren.equals(targetRealNodes)) // Must match targets exactly
                    .findFirst();

            Node bridgeNode;

            if (existingDummy.isPresent()) {
                // --- CASE A: REUSE EXISTING DUMMY ---
                bridgeNode = existingDummy.get();

                // Update relationships
                bridgeNode.parents.add(this);
                
                // Propagate "noDummyParents"
                if (this.node.isEmpty()) {
                    bridgeNode.noDummyParents.addAll(this.noDummyParents);
                } else {
                    this.node.ifPresent(bridgeNode.noDummyParents::add);
                }

                // We do NOT add longSpanningChildren to bridgeNode.children here, 
                // because if the dummy already exists, it is already connected to those paths.
                // However, we must ensure the *current* long children point to this bridge 
                // and disconnect from 'this'.
                
                for (Node child : longSpanningChildren) {
                    child.parents.remove(this);
                    // Note: Depending on your graph structure, you might need to ensure 'child' 
                    // is actually a child of 'bridgeNode'. In a standard reusable dummy scenario,
                    // it usually is. If 'bridgeNode' was created by a different parent pointing 
                    // to the SAME child instances, this is fine.
                    if (!bridgeNode.children.contains(child)) {
                        bridgeNode.children.add(child);
                        child.parents.add(bridgeNode);
                    }
                }

            } else {
                // --- CASE B: CREATE NEW DUMMY ---
                bridgeNode = new Node(null); // Empty Optional = Dummy
                
                // Register in LayerMap
                layerMap.put(bridgeNode, currentLayer + 1);

                // Set relationships
                bridgeNode.parents.add(this);
                bridgeNode.children.addAll(longSpanningChildren); // The dummy takes over these children
                
                // Set 'NoDummy' metadata
                bridgeNode.noDummyChildren.addAll(targetRealNodes);
                if (this.node.isEmpty()) {
                    bridgeNode.noDummyParents.addAll(this.noDummyParents);
                } else {
                    this.node.ifPresent(bridgeNode.noDummyParents::add);
                }

                // Update children's parents to point to the new dummy instead of 'this'
                for (Node child : longSpanningChildren) {
                    child.parents.remove(this);
                    child.parents.add(bridgeNode);
                }
            }

            // 4. Rewire 'this' node
            // Remove the long-spanning nodes from direct children
            this.children.removeAll(longSpanningChildren);
            // Add the bridge (dummy) as a direct child
            this.children.add(bridgeNode);

            // 5. Recursive Step
            // Iterate over the NEW state of children. 
            // This includes the 'bridgeNode' we just added. 
            // When 'bridgeNode' is processed, it will look at ITS children (the original longSpanningChildren).
            // If they are still far away (e.g., L1 -> L5), 'bridgeNode' (at L1) will create another dummy at L2.
            for (Node child : new ArrayList<>(this.children)) {
                child.recursiveMakeDummy(layerMap);
            }
        }


        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof Node otherNode) {
                if (this.node.isPresent() && otherNode.node.isPresent()) { // both not dummy nodes
                    return this.node.get().equals(otherNode.node.get());
                }
                if (this.node.isPresent() != otherNode.node.isPresent()) { // one dummy node, other not
                    return false;
                }
            }
            return false; // both dummies, or other is not Node instance
        }

        @Override
        public int hashCode() {
            return this.node.map(Object::hashCode).orElse(super.hashCode());
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
