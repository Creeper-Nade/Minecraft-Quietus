package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxGeometry;

public class TreeNodePosition {

    private Graph<SkillTreeNode, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    protected JGraphXAdapter<SkillTreeNode, DefaultEdge> adapter;

    public TreeNodePosition() {
        this.adapter = new JGraphXAdapter<>(this.graph);
    }

    public static void run(SkillCategory category) {
        TreeNodePosition instance = new TreeNodePosition();
        category.addJGraphVertexes(instance);

        mxHierarchicalLayout layout = new mxHierarchicalLayout(instance.adapter);
        layout.execute(instance.adapter.getDefaultParent());

        // Set node positions
        Map<SkillTreeNode, mxGeometry> vertexPositions = new HashMap<>();
        Object[] vertices = instance.adapter.getChildVertices(instance.adapter.getDefaultParent());
        for (Object vertex : vertices) {
            mxGeometry geometry = instance.adapter.getCellGeometry(vertex);
            if (geometry != null) {
                SkillTreeNode node = (SkillTreeNode)vertex;
                node.setTreePosition((int)(geometry.getX()*50), (int)(geometry.getY()*50));
            }
        }
    }

    public void addVertex(SkillTreeNode node) {
        this.graph.addVertex(node);
    }
    public void addEdge(SkillTreeNode source, SkillTreeNode target) {
        this.graph.addEdge(source, target);
    }
}
