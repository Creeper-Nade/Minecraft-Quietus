package com.minecraftquietus.quietus.skilltree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConnectivityPosition {
    private Map<Edge, List<Point>> edgeRoutes = new HashMap<>();

    public void addEdgeRoute(SkillTreeNode from, SkillTreeNode to, List<Point> route) {
        edgeRoutes.put(new Edge(from, to), route);
    }

    public List<Point> getEdgeRoute(SkillTreeNode from, SkillTreeNode to) {
        return edgeRoutes.get(new Edge(from, to));
    }

    public static class Edge {
        public final SkillTreeNode from;
        public final SkillTreeNode to;

        public Edge(SkillTreeNode from, SkillTreeNode to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }
}