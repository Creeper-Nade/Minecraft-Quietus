package com.quietus.skilltree;

import java.util.*;
import java.util.stream.Collectors;

public class LegacyPosition {

    private static final int HORIZONTAL_MARGIN = 12;
    private static final int VERTICAL_MARGIN = 12;
    private static final int EDGE_MARGIN = 4;

    private final int nodeWidth;
    private final int nodeHeight;

    public LegacyPosition(int nodeWidth, int nodeHeight) {
        this.nodeWidth = nodeWidth;
        this.nodeHeight = nodeHeight;
    }

    public ConnectivityPosition layout(Set<SkillTreeNode> allNodes) {
        ConnectivityPosition diagramLayout = new ConnectivityPosition();
        if (allNodes.isEmpty()) return diagramLayout;

        // Step 1: Assign layers using longest path from sources
        Map<SkillTreeNode, Integer> layerMap = assignLayers(allNodes);

        // Step 2: Group nodes by layer
        List<List<SkillTreeNode>> layers = groupNodesByLayer(allNodes, layerMap);

        // Step 3: Order nodes within layers to minimize edge crossings
        Map<SkillTreeNode, Integer> orderMap = orderNodes(layers, layerMap);

        // Step 4: Assign node coordinates
        assignNodeCoordinates(layers);

        // Step 5: Calculate edge routes with right angles
        calculateEdgeRoutes(allNodes, diagramLayout, layerMap);

        return diagramLayout;
    }

    private Map<SkillTreeNode, Integer> assignLayers(Set<SkillTreeNode> allNodes) {
        Map<SkillTreeNode, Integer> layerMap = new HashMap<>();
        Queue<SkillTreeNode> queue = new LinkedList<>();
        Map<SkillTreeNode, Integer> inDegree = new HashMap<>();

        for (SkillTreeNode node : allNodes) {
            inDegree.put(node, node.parents().size());
            if (node.parents().isEmpty()) {
                layerMap.put(node, 0);
                queue.add(node);
            }
        }

        while (!queue.isEmpty()) {
            SkillTreeNode current = queue.poll();
            for (SkillTreeNode child : current.children) {
                inDegree.put(child, inDegree.get(child) - 1);
                int proposedLayer = layerMap.get(current) + 1;
                /* Below code always leave the layerMap layer at maximum propsedLayer of its parents */
                if (proposedLayer > layerMap.getOrDefault(child, -1)) {
                    layerMap.put(child, proposedLayer);
                }
                if (inDegree.get(child) == 0) {
                    queue.add(child);
                }
            }
        }
        return layerMap;
    }

    private List<List<SkillTreeNode>> groupNodesByLayer(Set<SkillTreeNode> allNodes, Map<SkillTreeNode, Integer> layerMap) {
        int maxLayer = layerMap.values().stream().max(Integer::compare).orElse(0);
        List<List<SkillTreeNode>> layers = new ArrayList<>(maxLayer + 1);
        for (int i = 0; i <= maxLayer; i++) {
            layers.add(new ArrayList<>());
        }
        for (SkillTreeNode node : allNodes) {
            int layer = layerMap.get(node);
            layers.get(layer).add(node);
        }
        return layers;
    }

    private Map<SkillTreeNode, Integer> orderNodes(List<List<SkillTreeNode>> layers, Map<SkillTreeNode, Integer> layerMap) {
        Map<SkillTreeNode, Integer> orderMap = new HashMap<>();
        
        // Initialize first layer order
        for (int i = 0; i < layers.get(0).size(); i++) {
            orderMap.put(layers.get(0).get(i), i);
        }

        // Top-down pass
        for (int i = 1; i < layers.size(); i++) {
            final int layerIndex = i;
            List<SkillTreeNode> currentLayer = layers.get(i);
            Map<SkillTreeNode, Double> medianMap = new HashMap<>();
            for (SkillTreeNode node : currentLayer) {
                List<Integer> parentOrders = node.parents().stream()
                        .filter(parent -> layerMap.get(parent) == layerIndex - 1) // direct parent
                        .map(orderMap::get)
                        .sorted()
                        .collect(Collectors.toList());
                
                if (parentOrders.isEmpty()) {
                    medianMap.put(node, Double.MAX_VALUE);
                } else {
                    int mid = parentOrders.size() / 2;
                    double median = (parentOrders.size() % 2 == 1) ? 
                            parentOrders.get(mid) : 
                            (parentOrders.get(mid - 1) + parentOrders.get(mid)) / 2.0;
                    medianMap.put(node, median);
                }
            }
            currentLayer.sort(Comparator.comparingDouble(medianMap::get));
            for (int j = 0; j < currentLayer.size(); j++) {
                orderMap.put(currentLayer.get(j), j);
            }
        }

        // Bottom-up pass
        for (int i = layers.size() - 2; i >= 0; i--) {
            final int layerIndex = i;
            List<SkillTreeNode> currentLayer = layers.get(i);
            Map<SkillTreeNode, Double> medianMap = new HashMap<>();
            for (SkillTreeNode node : currentLayer) {
                List<Integer> childrenOrders = node.children.stream()
                        .filter(child -> layerMap.get(child) == layerIndex + 1)
                        .map(orderMap::get)
                        .sorted()
                        .collect(Collectors.toList());
                
                if (childrenOrders.isEmpty()) {
                    medianMap.put(node, Double.MAX_VALUE);
                } else {
                    int mid = childrenOrders.size() / 2;
                    double median = (childrenOrders.size() % 2 == 1) ? 
                            childrenOrders.get(mid) : 
                            (childrenOrders.get(mid - 1) + childrenOrders.get(mid)) / 2.0;
                    medianMap.put(node, median);
                }
            }
            currentLayer.sort(Comparator.comparingDouble(medianMap::get));
            for (int j = 0; j < currentLayer.size(); j++) {
                orderMap.put(currentLayer.get(j), j);
            }
        }
        
        return orderMap;
    }

    private void assignNodeCoordinates(List<List<SkillTreeNode>> layers) {
        int maxNodesInLayer = layers.stream().mapToInt(List::size).max().orElse(0);
        int maxWidth = maxNodesInLayer * (this.nodeWidth + HORIZONTAL_MARGIN) - HORIZONTAL_MARGIN;

        for (int i = 0; i < layers.size(); i++) {
            List<SkillTreeNode> layer = layers.get(i);
            int layerWidth = layer.size() * (this.nodeWidth + HORIZONTAL_MARGIN) - HORIZONTAL_MARGIN;
            int startX = (maxWidth - layerWidth) / 2;
            for (int j = 0; j < layer.size(); j++) {
                SkillTreeNode node = layer.get(j);
                int x = startX + j * (this.nodeWidth + HORIZONTAL_MARGIN);
                int y = i * (this.nodeHeight + VERTICAL_MARGIN);
                node.setTreeLocation(x, y);
            }
        }
    }

    private void calculateEdgeRoutes(Set<SkillTreeNode> allNodes, ConnectivityPosition diagramLayout, Map<SkillTreeNode, Integer> layerMap) {
        for (SkillTreeNode node : allNodes) {
            for (SkillTreeNode child : node.children) {
                List<Point> edgeRoute = calculateEdgeRoute(node, child, layerMap);
                diagramLayout.addEdgeRoute(node, child, edgeRoute);
            }
        }
    }

    private List<Point> calculateEdgeRoute(SkillTreeNode from, SkillTreeNode to, Map<SkillTreeNode, Integer> layerMap) {
        List<Point> points = new ArrayList<>();
        
        // Calculate port positions
        int fromBottomY = from.getTreeY() + this.nodeHeight;
        int toTopY = to.getTreeY();
        int fromCenterX = from.getTreeX() + this.nodeWidth / 2;
        int toCenterX = to.getTreeX() + this.nodeWidth / 2;
        
        // Start from bottom center of source node
        points.add(new Point(fromCenterX, fromBottomY));
        
        // If nodes are aligned, use direct vertical connection
        if (fromCenterX == toCenterX) {
            points.add(new Point(fromCenterX, toTopY));
        } else {
            // Add margin from source node
            int firstSegmentY = fromBottomY + EDGE_MARGIN;
            points.add(new Point(fromCenterX, firstSegmentY));
            
            // Calculate horizontal segment
            // Use layer gap to determine where to place horizontal segment
            int layerGap = layerMap.get(to) - layerMap.get(from);
            int horizontalSegmentY = fromBottomY + (VERTICAL_MARGIN * layerGap) / 2;
            
            points.add(new Point(fromCenterX, horizontalSegmentY));
            points.add(new Point(toCenterX, horizontalSegmentY));
            
            // Add margin to target node
            points.add(new Point(toCenterX, toTopY - EDGE_MARGIN));
            points.add(new Point(toCenterX, toTopY));
        }
        
        return points;
    }

    

    

    
}

