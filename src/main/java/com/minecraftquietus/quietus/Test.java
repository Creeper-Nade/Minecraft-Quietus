package com.minecraftquietus.quietus;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;

public class Test {

    Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public Graph<String, DefaultEdge> getGraph() {
        return this.graph;
    }

    public void makeGraph() {
        this.graph.addVertex("A");
        this.graph.addVertex("B");
        this.graph.addEdge("A", "B");

        // Create adapter for JGraphX
        JGraphXAdapter<String, DefaultEdge> graphAdapter = 
            new JGraphXAdapter<>(graph);
        
        // Apply hierarchical layout (good for parent-child relationships)
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        
        // Get node positions
        Map<String, mxGeometry> vertexPositions = new HashMap<>();
        Object[] vertices = graphAdapter.getChildVertices(graphAdapter.getDefaultParent());
        for (Object vertex : vertices) {
            mxGeometry geometry = graphAdapter.getCellGeometry(vertex);
            if (geometry != null) {
                String vertexLabel = graphAdapter.getLabel(vertex);
                vertexPositions.put(vertexLabel, geometry);
            }
        }
        

    }
}
